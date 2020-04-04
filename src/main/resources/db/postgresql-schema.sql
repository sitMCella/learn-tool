CREATE TABLE if not exists workspaces (
  name varchar (255) primary key
);

CREATE TABLE if not exists cards (
  id varchar (36) primary key,
  workspace_name varchar (255),
  question text NOT NULL,
  response text NOT NULL,
  CONSTRAINT fkWorkspaceName FOREIGN KEY (workspace_name) REFERENCES workspaces(name)
);
