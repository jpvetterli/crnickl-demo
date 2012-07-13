-- CREATE TABLES AND INDEXES
-- Idiom: HSQL
-- Table "series" must already have been defined because of referential integrity.
-- 2012-07-09/jp

create table value_geocoord (
  series int not null,
  date int not null,
  x double not null,
  y double not null,
  z double not null,
  constraint valgeo1 primary key (series, date),
  constraint valgeo2 foreign key (series) references series (id)
 );

