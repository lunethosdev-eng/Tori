-- Pokke Phase 3 — Kokoro favorites compatibility with Ukiyo UUID song IDs.
-- Applied to Supabase project ajbmpgnzkgtcmulocftd.
-- Preserve existing favorite identifiers while allowing UUID strings from Ukiyo.
ALTER TABLE public.favorites
  ALTER COLUMN song_id TYPE text
  USING song_id::text;

-- Required by the mobile app's favorite upsert (one favorite per user/song).
CREATE UNIQUE INDEX IF NOT EXISTS favorites_user_song_uidx
  ON public.favorites (user_id, song_id);
