-- coloane noi
ALTER TABLE public.cities
    ADD COLUMN IF NOT EXISTS center_lat    double precision,
    ADD COLUMN IF NOT EXISTS center_lng    double precision,
    ADD COLUMN IF NOT EXISTS bbox_min_lat  double precision,
    ADD COLUMN IF NOT EXISTS bbox_min_lng  double precision,
    ADD COLUMN IF NOT EXISTS bbox_max_lat  double precision,
    ADD COLUMN IF NOT EXISTS bbox_max_lng  double precision;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_cities_center_lat'
  ) THEN
ALTER TABLE public.cities
    ADD CONSTRAINT chk_cities_center_lat CHECK (
        center_lat IS NULL OR center_lat BETWEEN -90 AND 90
        );
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_cities_center_lng'
  ) THEN
ALTER TABLE public.cities
    ADD CONSTRAINT chk_cities_center_lng CHECK (
        center_lng IS NULL OR center_lng BETWEEN -180 AND 180
        );
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_cities_bbox_lat'
  ) THEN
ALTER TABLE public.cities
    ADD CONSTRAINT chk_cities_bbox_lat CHECK (
        (bbox_min_lat IS NULL OR bbox_max_lat IS NULL) OR (bbox_min_lat < bbox_max_lat)
        );
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_cities_bbox_lng'
  ) THEN
ALTER TABLE public.cities
    ADD CONSTRAINT chk_cities_bbox_lng CHECK (
        (bbox_min_lng IS NULL OR bbox_max_lng IS NULL) OR (bbox_min_lng < bbox_max_lng)
        );
END IF;
END $$;

-- index pe slug pentru cautari
CREATE UNIQUE INDEX IF NOT EXISTS uq_cities_slug ON public.cities (slug);

-- completeaza centrul din bbox, daca am bbox și lipseste centrul
UPDATE public.cities
SET center_lat = (bbox_min_lat + bbox_max_lat)/2.0,
    center_lng = (bbox_min_lng + bbox_max_lng)/2.0
WHERE center_lat IS NULL
  AND center_lng IS NULL
  AND bbox_min_lat IS NOT NULL AND bbox_max_lat IS NOT NULL
  AND bbox_min_lng IS NOT NULL AND bbox_max_lng IS NOT NULL;
