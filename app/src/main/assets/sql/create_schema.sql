PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS EntityJson(
entity_uuid TEXT,
entity_group TEXT,
entity_class TEXT,
entity_json TEXT,
sort_by BIGINT
);

CREATE TABLE IF NOT EXISTS FSVenue(
venue_id TEXT PRIMARY KEY NOT NULL,
name TEXT,
url TEXT,
rating BIGINT,
stats_checkinsCount INTEGER,
stats_usersCount INTEGER,
stats_tipsCount INTEGER,
price_tier INTEGER,
price_message TEXT,
price_currency TEXT,
hours_status TEXT,
hours_isOpen INTEGER,
hours_isLocalHoliday INTEGER,
hearNow_count INTEGER,
hearNow_summary TEXT,
isFavorite INTEGER
);

CREATE TABLE IF NOT EXISTS FSLocation(
entity_uuid TEXT,
address TEXT,
crossStreet TEXT,
lat BIGINT,
lng BIGINT,
distance INTEGER,
postalCode INTEGER,
cc TEXT,
city TEXT,
state TEXT,
country TEXT,
formattedAddress TEXT
);

CREATE TABLE IF NOT EXISTS FSCategory(
category_id TEXT PRIMARY KEY NOT NULL,
name TEXT,
pluralName TEXT,
shortName BIGINT,
icon_prefix TEXT,
icon_suffix TEXT,
isPrimary INTEGER
);

CREATE TABLE IF NOT EXISTS VenueCategoryAssociation(
lhs_uuid TEXT,
rhs_uuid TEXT
);

PRAGMA foreign_keys = OFF;
