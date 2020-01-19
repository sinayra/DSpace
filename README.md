
# AVISO

O README original deste projeto pode ser acessado no repositório original do [DSpace](https://github.com/DSpace/DSpace).

# Instalação
## Dependências
Antes de começar a instalar as dependências, certifique que seu computador esteja atualizado:
`sudo apt update`
`sudo apt upgrade`

### Dependências do Tematres
* Apache
* MySQL
* PHP e alguns plugins do PHP

#### Apache
`sudo apt install apache2`

#### MySQL
`sudo apt install mysql-server`
Em seguida, instale o MySQL e crie um usuário **root** para acessar o mysql.
`sudo mysql_secure_installation`
Crie um super-usuário **root** que seja capaz de fornecer privilégios para outras contas de usuário:
`mysql -u root -p`

```sql
DROP USER 'root'@'localhost';
CREATE USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

#### PHP
`sudo apt install php libapache2-mod-php php-mysql php7.3-mbstring php7.3-xml php7.3-gd php7.3-intl`

### Dependências do DSpace
* Java 8,Ant e Maven
* PostgreSQL
* Tomcat 8

#### Java, Ant e Maven
`sudo apt install openjdk-8-jdk ant maven`

#### PostgreSQL
`sudo apt install postgresql`

#### Tomcat
Baixe a versão mais recente do [Tomcat 8](https://tomcat.apache.org/download-80.cgi), descompacte-a e renomeia-a para *tomcat*. Em seguida, mova-a para a pasta */opt*.
Adicione no arquivo */etc/profile* as variáveis de ambiente:
```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export CATALINA_HOME=/opt/tomcat
```
## Tematres
Crie um usuário para acessar o banco do Tematres:
```sql
CREATE DATABASE tematres;
CREATE USER 'tematres'@'localhost' IDENTIFIED WITH mysql_native_password BY 'tematres';
GRANT ALL PRIVILEGES ON tematres.* TO 'tematres'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```
Baixe o [Tematres](https://sourceforge.net/projects/tematres/), descompacte-o, renomei-o para *tematres* e mova-o para */var/www/html*.
Em seguida, modifique os seguintes campos do arquivo */var/www/html/tematres/vocab/db.tematres.php*:
```php
//  Dirección IP o nombre del servidor - IP Address of the database server
$DBCFG["Server"]      = "localhost";
//  Nombre de la base de datos Database name
$DBCFG["DBName"]     = "tematres";
//  Nombre de usuario - login
$DBCFG["DBLogin"]    = "tematres";
//  Passwords
$DBCFG["DBPass"] = "tematres";
```
## DSpace
Crie um usuário para acessar o banco do DSpace:
```sql
CREATE DATABASE dspace;
CREATE USER dspace WITH ENCRYPTED PASSWORD 'dspace';
GRANT ALL PRIVILEGES ON DATABASE dspace TO dspace;
\c dspace;
CREATE EXTENSION pgcrypto;
```

Clone este repositório nesta branch:
`git clone --branch dspace-tematres https://github.com/esw-gama/DSpace.git`

Na pasta raiz, execute:
`
mvn -U package
`
Após este processo, na pasta */dspace/target/dspace-installer*, execute:
`
sudo ant fresh_install
`
Para finalizar a instalação, copie os webapps gerados para a pasta de webapps do Tomcat.