create table querytypes
     (id INTEGER,
      name VARCHAR(50) NOT NULL,
      PRIMARY KEY (id));
 
insert into querytypes(id,name) values (0,'navigational');
insert into querytypes(id,name) values (1,'informational');
 
create table queries
     (id INTEGER, -- query number
      query VARCHAR(100) NOT NULL,
      periodStart VARCHAR(100),
      periodEnd VARCHAR(100),
      description VARCHAR(500) NOT NULL,
      type INTEGER REFERENCES queryTypes (id),
      PRIMARY KEY (id),
      UNIQUE (query, type));
      
create table docs
     (id INTEGER,
      url VARCHAR(500) NOT NULL,
      date TIMESTAMP NOT NULL,
      urlarchived VARCHAR(600) NOT NULL,
      code VARCHAR(100) NOT NULL, -- document identifier
      PRIMARY KEY (id),
      UNIQUE (code));
--      UNIQUE (url, date));
                        
create table queriesdocs
    (id INTEGER,
     query INTEGER REFERENCES queries (id),
	 doc INTEGER REFERENCES docs (id),
	 features VARCHAR(1000) NOT NULL,	 
	 PRIMARY KEY (id),
	 UNIQUE (query, doc));
                        
create table assessments
    (querydoc INTEGER REFERENCES queriesdocs (id),	
	 userid VARCHAR(100) NOT NULL,
	 rate INTEGER NOT NULL,
	 comment VARCHAR(2000),
	 creationtime TIMESTAMP NOT NULL,
	 type INTEGER NOT NULL, -- 0 is manual; 1 is automatic
	 PRIMARY KEY (querydoc, userId));	

	 

-- delete from assessments;
-- delete from queriesdocs;
-- delete from docs;
-- delete from queries;



