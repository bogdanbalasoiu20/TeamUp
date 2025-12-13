alter type notification_type add value if not exists 'JOIN_REQUEST_RECEIVED';
alter type notification_type add value if not exists 'JOIN_REQUEST_ACCEPTED';
alter type notification_type add value if not exists 'JOIN_WAITLIST';
alter type notification_type add value if not exists 'PROMOTED_FROM_WAITLIST';
alter type notification_type add value if not exists 'MOVED_TO_WAITLIST';
alter type notification_type add value if not exists 'MATCH_LEFT';