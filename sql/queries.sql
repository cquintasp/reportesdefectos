--conexion
jdbc:postgresql://localhost:5432/mi_basedatos
user=admin
password=admin123

--tabla
CREATE TABLE public.defectos_tramites (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_tramite NUMERIC(10,0),
    id_bug NUMERIC(15,0),
    title VARCHAR(500), 
    state VARCHAR(30),
    creted_by VARCHAR(200),
    priority VARCHAR(20), 
    severity VARCHAR(20),
    bloque VARCHAR(30),
    atiende VARCHAr(100),
    fecha_reporte DATE,
    secuencia_reporte numeric(3)    
);



select distinct fecha_reporte, secuencia_reporte  from public.defectos_tramites

--OFFSHORE TO OFFSHORE

-- cuantos defectos por bloque1 primera secuencia
SELECT COUNT(*)
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
-- por dia

--cuantos defectos por tramite del bloque1 primera secuencia
SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

--cuantos defectos por tramite del bloque 1 y que defectos son  primera secuencia
SELECT tipo_tramite,
       COUNT(*) AS total_defectos,
       ARRAY_AGG(id_bug) AS defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

SELECT tipo_tramite, id_bug 
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
  order by tipo_tramite


-- cuantos defectos por bloque1 segunda secuencia
SELECT COUNT(*)
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=3
-- por dia

--cuantos defectos por tramite del bloque1
SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=3
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

--cuantos defectos por tramite del bloque 1 y que defectos son 
SELECT tipo_tramite,
       COUNT(*) AS total_defectos,
       ARRAY_AGG(id_bug) AS defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=3
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;


-- cuantos defectos por bloque  Bloque2groupE
SELECT COUNT(*)
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
  

-- por dia

--cuantos defectos por tramite del bloque1
SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

SELECT tipo_tramite,
       COUNT(*) AS total_defectos,
       ARRAY_AGG(id_bug) AS defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC

--AQUI

-- cuantos defectos por bloque  Bloque2groupE
SELECT COUNT(*)
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=3
  

-- por dia

--cuantos defectos por tramite del bloque1
SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=3
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;



SELECT tipo_tramite,
       COUNT(*) AS total_defectos,
       ARRAY_AGG(id_bug) AS defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=3
GROUP BY tipo_tramite
ORDER BY total_defectos desc

select count(*) from public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=3
  
  select tipo_tramite, id_bug  from public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ilink%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=3

  
  

--TERMINA AQUI

-----Ultrasist a offshore
-- cuantos defectos por bloque1 primera secuencia
SELECT COUNT(*)
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ultrasist%'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=1
-- por dia

--cuantos defectos por tramite del bloque1 primera secuencia
SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=1
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

--cuantos defectos por tramite del bloque 1 y que defectos son  primera secuencia
SELECT tipo_tramite,
       COUNT(*) AS total_defectos,
       ARRAY_AGG(id_bug) AS defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
 AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=1
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;


-- cuantos defectos por bloque1 segunda secuencia
SELECT COUNT(*)
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
-- por dia

--cuantos defectos por tramite del bloque1
SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

--cuantos defectos por tramite del bloque 1 y que defectos son 
SELECT tipo_tramite,
       COUNT(*) AS total_defectos,
       ARRAY_AGG(id_bug) AS defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque 1'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;


-- cuantos defectos por bloque  Bloque2groupE
SELECT COUNT(*)
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
 AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=1
  

-- por dia

--cuantos defectos por tramite del bloque1
SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
  AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=1
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

SELECT tipo_tramite,
       COUNT(*) AS total_defectos,
       ARRAY_AGG(id_bug) AS defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=1
GROUP BY tipo_tramite
ORDER BY total_defectos DESC

--AQUI

-- cuantos defectos por bloque  Bloque2groupE
SELECT COUNT(*)
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
  

-- por dia

--cuantos defectos por tramite del bloque1
SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

SELECT tipo_tramite, COUNT(*) AS total_defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
  and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC;

SELECT tipo_tramite,
       COUNT(*) AS total_defectos,
       ARRAY_AGG(id_bug) AS defectos
FROM public.defectos_tramites
WHERE bloque = 'Bloque2groupE'
  AND severity = '1 - Critical'
  AND atiende = 'iLink'
AND creted_by ILIKE '%ultrasist%'
  AND creted_by <> 'llealg@ilink-systems.com'
   and fecha_reporte='2026-02-23'
  and secuencia_reporte=2
GROUP BY tipo_tramite
ORDER BY total_defectos DESC
--TERMINA AQUI







WITH hoy AS (
    SELECT tipo_tramite, id_bug
    FROM public.defectos_tramites
    WHERE bloque = 'Bloque 1'
      AND severity = '1 - Critical'
      AND atiende = 'iLink'
      AND creted_by ILIKE '%ilink%'
      AND creted_by <> 'llealg@ilink-systems.com'
      AND fecha_reporte = CURRENT_DATE
),
ayer AS (
    SELECT tipo_tramite, id_bug
    FROM public.defectos_tramites
    WHERE bloque = 'Bloque 1'
      AND severity = '1 - Critical'
      AND atiende = 'iLink'
      AND creted_by ILIKE '%ilink%'
      AND creted_by <> 'llealg@ilink-systems.com'
      AND fecha_reporte = CURRENT_DATE - INTERVAL '1 day'
)

SELECT 
    COALESCE(hoy.tipo_tramite, ayer.tipo_tramite) AS tipo_tramite,
    
    -- Bugs nuevos hoy
    ARRAY_AGG(DISTINCT hoy.id_bug) FILTER (WHERE hoy.id_bug IS NOT NULL AND ayer.id_bug IS NULL) AS bugs_agregados,
    
    -- Bugs que ya no están hoy
    ARRAY_AGG(DISTINCT ayer.id_bug) FILTER (WHERE ayer.id_bug IS NOT NULL AND hoy.id_bug IS NULL) AS bugs_eliminados

FROM hoy
FULL OUTER JOIN ayer
    ON hoy.tipo_tramite = ayer.tipo_tramite
    AND hoy.id_bug = ayer.id_bug
GROUP BY COALESCE(hoy.tipo_tramite, ayer.tipo_tramite)
ORDER BY tipo_tramite;


-- por secuencia 
WITH ranked AS (
    SELECT *,
           ROW_NUMBER() OVER (
               PARTITION BY tipo_tramite
               ORDER BY secuencia_reporte DESC
           ) AS rn
    FROM public.defectos_tramites
    WHERE fecha_reporte = CURRENT_DATE
      AND bloque = 'Bloque 1'
      AND severity = '1 - Critical'
      AND atiende = 'iLink'
      AND creted_by ILIKE '%ilink%'
      AND creted_by <> 'llealg@ilink-systems.com'
),
ultima AS (
    SELECT tipo_tramite, id_bug
    FROM ranked
    WHERE rn = 1
),
segunda AS (
    SELECT tipo_tramite, id_bug
    FROM ranked
    WHERE rn = 2
)

-- Bugs agregados: si no existe segunda secuencia, todos de última secuencia son agregados
SELECT u.tipo_tramite,
       u.id_bug,
       'agregado' AS estado
FROM ultima u
LEFT JOIN segunda s
       ON u.tipo_tramite = s.tipo_tramite
      AND u.id_bug = s.id_bug
WHERE s.id_bug IS NULL

UNION ALL

-- Bugs eliminados: solo si existe segunda secuencia
SELECT s.tipo_tramite,
       s.id_bug,
       'eliminado' AS estado
FROM segunda s
LEFT JOIN ultima u
       ON s.tipo_tramite = u.tipo_tramite
      AND s.id_bug = u.id_bug
WHERE u.id_bug IS NULL

ORDER BY tipo_tramite, estado DESC, id_bug;

---

WITH secuencias AS (
    SELECT tipo_tramite,
           secuencia_reporte,
           fecha_reporte
    FROM public.defectos_tramites
    WHERE fecha_reporte = CURRENT_DATE
      AND bloque = 'Bloque 1'
      AND severity = '1 - Critical'
      AND atiende = 'iLink'
      AND creted_by ILIKE '%ilink%'
      AND creted_by <> 'llealg@ilink-systems.com'
    GROUP BY tipo_tramite, secuencia_reporte, fecha_reporte
),
ultima AS (
    SELECT tipo_tramite, id_bug
    FROM public.defectos_tramites dt
    WHERE fecha_reporte = CURRENT_DATE
      AND (tipo_tramite, secuencia_reporte) IN (
          SELECT tipo_tramite, MAX(secuencia_reporte)
          FROM secuencias
          GROUP BY tipo_tramite
      )
    GROUP BY tipo_tramite, id_bug
),
segunda AS (
    SELECT tipo_tramite, id_bug
    FROM public.defectos_tramites dt
    WHERE fecha_reporte = CURRENT_DATE
      AND (tipo_tramite, secuencia_reporte) IN (
          SELECT tipo_tramite, MAX(secuencia_reporte)
          FROM secuencias
          WHERE secuencia_reporte < (
              SELECT MAX(secuencia_reporte)
              FROM secuencias s2
              WHERE s2.tipo_tramite = secuencias.tipo_tramite
          )
          GROUP BY tipo_tramite
      )
    GROUP BY tipo_tramite, id_bug
)

-- Bugs agregados
SELECT u.tipo_tramite,
       COUNT(u.id_bug) AS bugs_agregados,
       0 AS bugs_eliminados
FROM ultima u
LEFT JOIN segunda s
  ON u.tipo_tramite = s.tipo_tramite
 AND u.id_bug = s.id_bug
WHERE s.id_bug IS NULL
GROUP BY u.tipo_tramite

UNION ALL

-- Bugs eliminados
SELECT s.tipo_tramite,
       0 AS bugs_agregados,
       COUNT(s.id_bug) AS bugs_eliminados
FROM segunda s
LEFT JOIN ultima u
  ON s.tipo_tramite = u.tipo_tramite
 AND s.id_bug = u.id_bug
WHERE u.id_bug IS NULL
GROUP BY s.tipo_tramite

ORDER BY tipo_tramite;
