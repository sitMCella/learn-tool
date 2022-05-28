CREATE TABLE if not exists users (
  id bigserial primary key,
  name varchar (255),
  email varchar (36) NOT NULL,
  image_url varchar (255),
  email_verified boolean,
  password varchar (255) NOT NULL,
  auth_provider varchar (36) NOT NULL,
  auth_provider_id varchar (36) NOT NULL
);

CREATE TABLE if not exists workspaces (
  id varchar (36) primary key,
  name varchar (255) NOT NULL,
  user_id bigserial NOT NULL,
  CONSTRAINT fkUserId FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE if not exists cards (
  id varchar (36) primary key,
  workspace_id varchar (36),
  question text NOT NULL,
  response text NOT NULL,
  creation_date timestamp NOT NULL,
  CONSTRAINT fkWorkspaceId FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);

CREATE TABLE if not exists learn_cards (
  id varchar (36) primary key references cards,
  workspace_id varchar (36),
  last_review timestamp NOT NULL,
  next_review timestamp NOT NULL,
  repetitions integer NOT NULL,
  ease_factor real NOT NULL,
  interval_days integer NOT NULL,
  CONSTRAINT fkWorkspaceId FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);
