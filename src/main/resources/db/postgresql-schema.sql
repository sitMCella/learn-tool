CREATE TABLE if not exists workspaces (
  name varchar (255) primary key
);

CREATE TABLE if not exists cards (
  id uuid primary key,
  question text NOT NULL,
  response text NOT NULL
);
