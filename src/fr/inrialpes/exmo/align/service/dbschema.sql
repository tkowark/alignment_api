// alignment info

create table alignment (
   id varchar(50), 
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
   id varchar(50),
   cell_id varchar(20),
   uri1 varchar(100),
   uri2 varchar(100),
   semantics varchar(30),
   measure varchar(20),
   relation varchar(5));


// cell method

create table method(
   id varchar(50),
   tag varchar(20),
   extension varchar(100));