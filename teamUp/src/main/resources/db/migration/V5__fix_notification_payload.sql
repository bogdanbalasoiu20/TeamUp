alter table notifications
alter column payload
set data type jsonb
using payload::jsonb