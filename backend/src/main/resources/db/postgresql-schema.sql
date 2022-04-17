CREATE TABLE if not exists workspaces (
  name varchar (255) primary key
);

CREATE TABLE if not exists cards (
  id varchar (36) primary key,
  workspace_name varchar (255),
  question text NOT NULL,
  response text NOT NULL,
  creation_date timestamp NOT NULL,
  CONSTRAINT fkWorkspaceName FOREIGN KEY (workspace_name) REFERENCES workspaces(name)
);

CREATE TABLE if not exists learn_cards (
  id varchar (36) primary key references cards,
  workspace_name varchar (255),
  last_review timestamp NOT NULL,
  next_review timestamp NOT NULL,
  repetitions integer NOT NULL,
  ease_factor real NOT NULL,
  interval_days integer NOT NULL
)
