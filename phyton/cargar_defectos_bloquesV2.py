import csv
import sys
import os
from datetime import date
from collections import defaultdict

def limpiar_tipo_tramite(title):
    if not title:
        return '0'
    tipo = title[:6]
    tipo = tipo.replace('"', '').replace(" ", "").replace(":", "")
    if not tipo.isdigit():
        return '0'
    return tipo

def escapar(valor):
    if valor is None or valor == '':
        return ''
    return str(valor).replace("'", "''")

def determinar_atiende(assigned_to):
    if not assigned_to:
        return ''
    texto = assigned_to.lower()
    if 'ultrasist' in texto or 'cquintasp' in texto or 'chandra.quintas' in texto: 
        return 'Ultrasist'
    elif 'ilink' in texto:
        return 'iLink'
    else:
        return ''

def leer_bloques(ruta_bloques):
    bloques_map = {}

    if not os.path.exists(ruta_bloques):
        print(f"⚠️ No se encontró el archivo de bloques: {ruta_bloques}")
        return bloques_map

    for encoding in ['utf-8', 'cp1252']:
        try:
            with open(ruta_bloques, newline='', encoding=encoding) as f:
                reader = csv.DictReader(f)
                reader.fieldnames = [h.strip().lower() for h in reader.fieldnames]

                for row_num, row in enumerate(reader, start=1):
                    key = row.get("tramite")
                    val = row.get("bloque")

                    if key:
                        bloques_map[key] = val

                    print(f"Línea {row_num} bloques.csv: TipoTramite={key}, Bloque={val}")
            break
        except UnicodeDecodeError:
            continue

    return bloques_map


def main():

    if len(sys.argv) != 4:
        print("Uso correcto:")
        print("python cargar_defectos.py <archivo_csv_origen> <archivo_bloques_csv> <secuencia>")
        sys.exit(1)

    csv_file = sys.argv[1]
    bloques_file = sys.argv[2]
    secuencia_reporte = sys.argv[3]

    if not secuencia_reporte.isdigit():
        print("❌ La secuencia debe ser numérica.")
        sys.exit(1)

    if not os.path.exists(csv_file):
        print(f"❌ El archivo no existe: {csv_file}")
        sys.exit(1)

    base_name = os.path.splitext(csv_file)[0]
    output_sql_file = base_name + ".sql"
    fecha_reporte = date.today().strftime('%Y-%m-%d')

    valores = []
    bloques_map = leer_bloques(bloques_file)

    print(f"\n📄 Bloques cargados: {len(bloques_map)}")
    print(f"🔢 Secuencia usada: {secuencia_reporte}")

    resumen_bloques = defaultdict(int)

    for encoding in ['utf-8', 'cp1252']:
        try:
            with open(csv_file, newline='', encoding=encoding) as csvfile:
                reader = csv.DictReader(csvfile)

                columnas_requeridas = [
                    "ID", "Title", "State", "Created By",
                    "Priority", "Severity", "Assigned To"
                ]

                for col in columnas_requeridas:
                    if col not in reader.fieldnames:
                        print(f"❌ Falta la columna requerida: {col}")
                        print("Columnas encontradas:", reader.fieldnames)
                        sys.exit(1)

                for row_num, row in enumerate(reader, start=1):

                    id_bug = escapar(row.get("ID"))
                    title = row.get("Title")
                    state = escapar(row.get("State"))
                    created_by = escapar(row.get("Created By"))
                    priority = escapar(row.get("Priority"))
                    severity = escapar(row.get("Severity"))
                    assigned_to = row.get("Assigned To")

                    tipo_tramite = limpiar_tipo_tramite(title)
                    bloque = bloques_map.get(tipo_tramite, '')
                    atiende = determinar_atiende(assigned_to)

                   # print(f"Linea {row_num}: ID={id_bug}, Tipo={tipo_tramite}, Bloque={bloque}, Atiende={atiende}")

                    resumen_bloques[bloque] += 1

                    valores.append(
                        "('{}','{}','{}','{}','{}','{}','{}','{}','{}','{}')".format(
                            tipo_tramite,
                            id_bug,
                            state,
                            created_by,
                            priority,
                            severity,
                            bloque,
                            atiende,
                            fecha_reporte,
                            secuencia_reporte
                        )
                    )

            break
        except UnicodeDecodeError:
            continue

    if not valores:
        print("⚠️ No hay registros para insertar.")
        sys.exit(0)

    print("\n📊 Resumen por bloque:")
    for bloque, count in resumen_bloques.items():
        print(f"{bloque if bloque else '(vacío)'}: {count}")

    insert_final = """
INSERT INTO public.defectos_tramites
(tipo_tramite, id_bug, state, creted_by, priority, severity,
 bloque, atiende, fecha_reporte, secuencia_reporte)
VALUES
{};
""".format(",\n".join(valores))

    with open(output_sql_file, 'w', encoding='utf-8') as sqlfile:
        sqlfile.write(insert_final)

    print("\n✅ Archivo SQL generado correctamente.")
    print(f"📄 Archivo creado: {output_sql_file}")


if __name__ == "__main__":
    main()