BEGIN;

-- 0) Tabele interne/auxiliare: blochează-le în API
REVOKE ALL ON TABLE public.flyway_schema_history FROM anon, authenticated;
REVOKE ALL ON TABLE public.test_table            FROM anon, authenticated;
REVOKE ALL ON TABLE public.spatial_ref_sys       FROM anon, authenticated;

-- 1) ENABLE RLS (safe dacă e deja activat)
ALTER TABLE public.users                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.venues                ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.matches               ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.match_participants    ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.match_chat_messages   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.friend_requests       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.friendships           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.skills                ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.skill_endorsements    ENABLE ROW LEVEL SECURITY;

-- 2) POLITICI (create doar dacă nu există)
DO $POLICY$
BEGIN
  ---------------------------------------------------------------------------
  -- users
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='users' AND policyname='users_select_auth'
  ) THEN
    CREATE POLICY "users_select_auth"
      ON public.users
      FOR SELECT TO authenticated
                              USING (true);
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='users' AND policyname='users_update_own'
  ) THEN
    CREATE POLICY "users_update_own"
      ON public.users
      FOR UPDATE TO authenticated
                              USING (id = auth.uid())
          WITH CHECK (id = auth.uid());
END IF;

  ---------------------------------------------------------------------------
  -- venues
  -- (ai cerut acces PUBLIC: anon poate citi)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='venues' AND policyname='venues_read_all_anon'
  ) THEN
    CREATE POLICY "venues_read_all_anon"
      ON public.venues
      FOR SELECT TO anon
                              USING (true);
END IF;

  ---------------------------------------------------------------------------
  -- matches (coloana: creator_user_id)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='matches' AND policyname='matches_select_visible'
  ) THEN
    CREATE POLICY "matches_select_visible"
      ON public.matches
      FOR SELECT TO authenticated
                              USING (
                              creator_user_id = auth.uid()
                              OR EXISTS (
                              SELECT 1 FROM public.match_participants mp
                              WHERE mp.match_id = matches.id
                              AND mp.user_id = auth.uid()
                              )
                              );
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='matches' AND policyname='matches_insert_self'
  ) THEN
    CREATE POLICY "matches_insert_self"
      ON public.matches
      FOR INSERT TO authenticated
      WITH CHECK (creator_user_id = auth.uid());
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='matches' AND policyname='matches_update_own'
  ) THEN
    CREATE POLICY "matches_update_own"
      ON public.matches
      FOR UPDATE TO authenticated
                              USING (creator_user_id = auth.uid())
          WITH CHECK (creator_user_id = auth.uid());
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='matches' AND policyname='matches_delete_own'
  ) THEN
    CREATE POLICY "matches_delete_own"
      ON public.matches
      FOR DELETE TO authenticated
      USING (creator_user_id = auth.uid());
END IF;

  ---------------------------------------------------------------------------
  -- match_participants (coloane: match_id, user_id)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='match_participants' AND policyname='mp_select'
  ) THEN
    CREATE POLICY "mp_select"
      ON public.match_participants
      FOR SELECT TO authenticated
                              USING (
                              user_id = auth.uid()
                              OR EXISTS (
                              SELECT 1 FROM public.matches m
                              WHERE m.id = match_participants.match_id
                              AND m.creator_user_id = auth.uid()
                              )
                              );
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='match_participants' AND policyname='mp_insert_self'
  ) THEN
    CREATE POLICY "mp_insert_self"
      ON public.match_participants
      FOR INSERT TO authenticated
      WITH CHECK (user_id = auth.uid());
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='match_participants' AND policyname='mp_delete_self_or_owner'
  ) THEN
    CREATE POLICY "mp_delete_self_or_owner"
      ON public.match_participants
      FOR DELETE TO authenticated
      USING (
        user_id = auth.uid()
        OR EXISTS (
          SELECT 1 FROM public.matches m
          WHERE m.id = match_participants.match_id
            AND m.creator_user_id = auth.uid()
        )
      );
END IF;

  ---------------------------------------------------------------------------
  -- match_chat_messages (coloane: match_id, sender_id)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='match_chat_messages' AND policyname='chat_select_participants'
  ) THEN
    CREATE POLICY "chat_select_participants"
      ON public.match_chat_messages
      FOR SELECT TO authenticated
                              USING (
                              EXISTS (
                              SELECT 1 FROM public.match_participants mp
                              WHERE mp.match_id = match_chat_messages.match_id
                              AND mp.user_id  = auth.uid()
                              )
                              OR EXISTS (
                              SELECT 1 FROM public.matches m
                              WHERE m.id = match_chat_messages.match_id
                              AND m.creator_user_id = auth.uid()
                              )
                              );
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='match_chat_messages' AND policyname='chat_insert_participant'
  ) THEN
    CREATE POLICY "chat_insert_participant"
      ON public.match_chat_messages
      FOR INSERT TO authenticated
      WITH CHECK (
        sender_id = auth.uid() AND
        (
          EXISTS (
            SELECT 1 FROM public.match_participants mp
            WHERE mp.match_id = match_chat_messages.match_id
              AND mp.user_id  = auth.uid()
          )
          OR EXISTS (
            SELECT 1 FROM public.matches m
            WHERE m.id = match_chat_messages.match_id
              AND m.creator_user_id = auth.uid()
          )
        )
      );
END IF;

  ---------------------------------------------------------------------------
  -- friend_requests (coloane: requester_id, addressee_id)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='friend_requests' AND policyname='fr_select_parties'
  ) THEN
    CREATE POLICY "fr_select_parties"
      ON public.friend_requests
      FOR SELECT TO authenticated
                              USING (requester_id = auth.uid() OR addressee_id = auth.uid());
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='friend_requests' AND policyname='fr_insert_self'
  ) THEN
    CREATE POLICY "fr_insert_self"
      ON public.friend_requests
      FOR INSERT TO authenticated
      WITH CHECK (requester_id = auth.uid() AND requester_id <> addressee_id);
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='friend_requests' AND policyname='fr_update_receiver'
  ) THEN
    CREATE POLICY "fr_update_receiver"
      ON public.friend_requests
      FOR UPDATE TO authenticated
                              USING (addressee_id = auth.uid())
          WITH CHECK (addressee_id = auth.uid());
END IF;

  ---------------------------------------------------------------------------
  -- friendships (coloane: user_a, user_b)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='friendships' AND policyname='fs_select_members'
  ) THEN
    CREATE POLICY "fs_select_members"
      ON public.friendships
      FOR SELECT TO authenticated
                              USING (user_a = auth.uid() OR user_b = auth.uid());
END IF;

  ---------------------------------------------------------------------------
  -- notifications (coloană: user_id)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='notifications' AND policyname='notif_select_owner'
  ) THEN
    CREATE POLICY "notif_select_owner"
      ON public.notifications
      FOR SELECT TO authenticated
                              USING (user_id = auth.uid());
END IF;

  ---------------------------------------------------------------------------
  -- skills (read pentru autentificați)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='skills' AND policyname='skills_read_all'
  ) THEN
    CREATE POLICY "skills_read_all"
      ON public.skills
      FOR SELECT TO authenticated
                              USING (true);
END IF;

  ---------------------------------------------------------------------------
  -- skill_endorsements (coloane: target_user_id, voter_user_id, skill_code)
  ---------------------------------------------------------------------------
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='skill_endorsements' AND policyname='endorse_read_all'
  ) THEN
    CREATE POLICY "endorse_read_all"
      ON public.skill_endorsements
      FOR SELECT TO authenticated
                              USING (true);
END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE schemaname='public' AND tablename='skill_endorsements' AND policyname='endorse_insert_self'
  ) THEN
    CREATE POLICY "endorse_insert_self"
      ON public.skill_endorsements
      FOR INSERT TO authenticated
      WITH CHECK (
        voter_user_id  = auth.uid()
        AND voter_user_id <> target_user_id
      );
END IF;

END
$POLICY$;

COMMIT;
