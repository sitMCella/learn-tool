# Virtual Machine Deployment

The following document describes the procedure for configuring and deploying the Learn Tool application on Ubuntu virtual machines.

The procedure assumes one virtual machine for the application and one for the database. It has been tested on the Ubuntu 24.04 Minimal image.

# Database Virtual Machine

Requirements:
- Create the virtual machine in a subnet and allow inbound traffic on port 5432 to the virtual machine.

Assumptions:
- Both Virtual Machines are located in the same subnet.
- The Ubuntu Firewall ufw is not enabled in the Virtual Machine.

## Install PostgreSQL Server

```sh
sudo apt update
sudo apt install postgresql-16
psql -V
sudo systemctl status postgresql # service postgresql status
```

## Configure PostgreSQL Server

```sh
sudo -u postgres psql
\password postgres
# [configure the password]
\q

sudo -u postgres psql -c "SHOW config_file;"
sudo nano /etc/postgresql/16/main/postgresql.conf

# Update the value of listen_address:
# listen_addresses = '*'

sudo nano /etc/postgresql/16/main/pg_hba.conf
# Replace the line:
# local   all             postgres                           locale
# with:
# local   all             all                                md5

# Add the following line:
# host    all             all             <IP_Address_range_subnet>             md5

sudo systemctl restart postgresql.service
```

## Configure PostgreSQL Database

```sh
sudo -u postgres psql

CREATE DATABASE "learn-tool" WITH OWNER = postgres ENCODING = 'UTF8' LOCALE = 'C.UTF-8' LC_COLLATE = 'C.UTF-8' LC_CTYPE = 'C.UTF-8' TEMPLATE = template0;
\l
\c "learn-tool"
CREATE SCHEMA postgres AUTHORIZATION postgres;
CREATE USER "user" WITH PASSWORD 'your_secure_password';
CREATE ROLE contributor;
GRANT USAGE, CREATE ON SCHEMA public TO contributor;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO contributor;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO contributor;
GRANT contributor TO "user";
\du
\q

psql -U user -d learn-tool
```

## Expose Server port 5432

```sh
sudo ss -tulpn
sudo iptables -I INPUT 2 -p tcp -s <IP_address_application_virtual_machine> --dport 5432 -j ACCEPT
```

# Application Virtual Machine

Requirements:
- Create the virtual machine in a subnet and allow inbound traffic on port 80 to the virtual machine.

Assumptions:
- Both Virtual Machines are located in the same subnet.
- The Ubuntu Firewall ufw is not enabled in the Virtual Machine.

## Install Git

```sh
sudo apt-get update
sudo apt install git
git --version
```

## Install OpenJDK 11

```sh
sudo apt install openjdk-11-jdk
javac --version
java --version
```

## Install NVM https://github.com/nvm-sh/nvm

```sh
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
source ~/.bashrc
```

## Install NodeJS

```sh
nvm --version
nvm install 12
nvm list
nvm use 12
node --version
```

## Clone Git Repository

```sh
git clone https://github.com/sitMCella/learn-tool.git
```

## Configure Gradle

```sh
sudo apt install nano

nano ~/.gradle/gradle.properties
```

Add the following configuration:

```sh
# Reduce memory usage
org.gradle.jvmargs=-Xmx512m -Xms128m -XX:+UseSerialGC

# Disable Gradle daemon to save memory
org.gradle.daemon=false

# Limit parallel workers
org.gradle.workers.max=1

# Disable build scan and caching for simplicity
org.gradle.caching=false
```

## Build Application

```sh
cd learn-tool

cd frontend
export NODE_OPTIONS="--max-old-space-size=512"
npm install
npm run build

cd ..
./gradlew clean build -PskipWebApp -Dorg.gradle.jvmargs="-Xmx512m -Xms128m -XX:+UseSerialGC"
cd ~
```

## Test PostgreSQL connection

```sh
sudo apt install postgresql-client -y

psql -h <IP_address_databasevirtual_machine> -p 5432 -U user -d learn-tool
```

## Run Backend Application

```sh
export SPRING_DATASOURCE_URL=jdbc:postgresql://<IP_address_databasevirtual_machine>:5432/learn-tool
export SPRING_DATASOURCE_USERNAME=user
export SPRING_DATASOURCE_PASSWORD=<database_user_password>
export SPRING_DATASOURCE_PLATFORM=postgresql
export SPRING_DATASOURCE_INITIALIZATION_MODE=always
export SPRING_DATASOURCE_INITIALIZE=1
export SPRING_DATASOURCE_SCHEMA=classpath:/db/postgresql-schema.sql
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
export SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
export SPRING_JPA_HIBERNATE_DLL_AUTO=none
export SPRING_JPA_HIBERNATE_SHOW_SQL=0

java -jar ~/learn-tool/backend/build/libs/backend-0.0.1-SNAPSHOT.jar -Dorg.gradle.jvmargs="-Xmx768m -Xms128m -XX:+UseSerialGC" > /dev/null 2>&1 &

ps aux | grep java

less logs/spring-boot-logger.log
```

## Expose Server port 80

```sh
sudo ss -tulpn
sudo iptables -I INPUT 2 -p tcp --dport 80 -j ACCEPT
```

## Install NGINX

```sh
sudo apt install nginx
```

## Deploy Fronted Application

```sh
ls -l /etc/nginx/sites-enabled/ # default -> /etc/nginx/sites-available/default

sudo cp -r ~/learn-tool/frontend/build/* /usr/share/nginx/html

sudo cp ~/learn-tool/frontend/nginx.conf /etc/nginx/conf.d

sudo  nano /etc/nginx/conf.d/nginx.conf

Update the parameter "proxy_pass" as follows:
proxy_pass http://127.0.0.1:8080;

sudo nano /etc/nginx/sites-available/default
```

Configure the following:

```
server {
        listen 80 default_server;
        listen [::]:80 default_server;

        root /usr/share/nginx/html;
        index index.html;

        location / {
                try_files $uri /index.html;
        }

        location /api/ {
                proxy_pass http://127.0.0.1:8080;
                proxy_http_version 1.1;
                proxy_set_header Upgrade $http_upgrade;
                proxy_set_header Connection 'upgrade';
                proxy_set_header Host $host;
                proxy_cache_bypass $http_upgrade;
        }

        # Allow all access to /api/auth
        location /api/auth {
                # No authentication, allow all
                allow all;
                proxy_pass http://127.0.0.1:8080/api/auth;
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }
}
```

Restart NGINX:

```sh
sudo nginx -t
sudo service nginx restart
sudo systemctl status nginx
```
