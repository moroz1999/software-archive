-- The page an imported entry has in its source archive, for a link back to it; a name taken as a label has none.
ALTER TABLE external_origin ADD COLUMN url varchar(2048);
