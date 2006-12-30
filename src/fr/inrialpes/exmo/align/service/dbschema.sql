#
# Create the database. If you change any value here, report
# the changes in the DBInfo.php file.
#

CREATE DATABASE AServDB;

#
# Create a MySQL user. Change 'localhost' to the name of the server
# that hosts MySQL.
#

GRANT ALL PRIVILEGES ON AServDB.* TO adminAServ@localhost
       IDENTIFIED BY 'aaa345';

#
# Create a MySQL user with restricted right for SQL queries
#

GRANT select ON AServ.*  TO alignserver@localhost IDENTIFIED BY 'adf342';

// alignment info

create table alignment (
   id varchar(100), 
   owlontology1 varchar(100),
   owlontology2 varchar(100),
   type varchar(5),
   level varchar(1),
   file1 varchar(100),
   file2 varchar(100),
   uri1 varchar(100),
   uri2 varchar(100));


// cell info

create table cell(
   id varchar(100),
   cell_id varchar(20),
   uri1 varchar(100),
   uri2 varchar(100),
   semantics varchar(30),
   measure varchar(20),
   relation varchar(5));


// extension info

create table extension(
   id varchar(100),
   tag varchar(100),
   method varchar(500));

