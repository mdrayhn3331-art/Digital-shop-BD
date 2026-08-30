// RSM SHOP - Supabase client
// IMPORTANT: Replace the placeholders below with your Supabase project's URL and publishable (anon) key.
// Never put a Supabase service_role/secret key in frontend code.
const SUPABASE_URL = 'YOUR_SUPABASE_URL';
const SUPABASE_PUBLISHABLE_KEY = 'YOUR_SUPABASE_PUBLISHABLE_KEY';

window.rsmSupabaseConfig = {
  url: SUPABASE_URL,
  key: SUPABASE_PUBLISHABLE_KEY
};

// This file is intentionally configuration-only until the real project URL/key are supplied.
// The RSM SHOP frontend can then initialize @supabase/supabase-js and use Auth/Database.
