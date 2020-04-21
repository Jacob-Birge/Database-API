use SMPDB;
delete from Identity;
delete from Story;
delete from Follows;
delete from Reprint;
delete from Block;

insert into Identity(handle, pass, fullname, location, email, bdate, joined)
values
	("@kylehummie", "poopie", "Kyle Hume", "Kentucky", "kyle@gmail.com", "1998-09-24", "2010-01-01 00:00:01"),
	("@sammieboy", "minecraft", "Sam Armstrong", "Kentucky", "sam@gmail.com", "1999-02-15", "2010-01-01 00:00:01"),
	("@thekelsey", "samsucks", "Kelsey Cole", "Kentucky", "kelsey@gmail.com", "1998-02-24", "2010-01-01 00:00:01"),
	("@birgeboy", "pass1234", "Jacob Birge", "Kentucky", "jacob@gmail.com", "1999-01-08", "2010-01-01 00:00:01"),
	("@alexis", "redsocksfan", "Alex Lucas", "Kentucky", "alex@gmail.com", "1999-09-24", "2010-01-01 00:00:01"),
	("@trey", "pass4321", "Trey Lastname", "Kentucky", "trey@gmail.com", "1999-05-17", "2010-01-01 00:00:01");
insert into Identity(handle, pass, fullname, email, bdate, joined)
values
	("@tyguy", "mechanicman", "Ty Birge", "ty@gmail.com", "2001-01-09", "2010-01-01 00:00:01");

insert into Story(idnum, chapter, url, expires, tstamp)
values
	(1, "First Post", "sup.org", "2010-01-01 01:01:0", "2010-01-01 00:01:00");

insert into Follows(follower, followed, tstamp)
values
	(1, 2, "2010-01-01 00:00:01"),
	(1, 3, "2010-01-01 00:00:01"),
	(2, 1, "2010-01-01 00:00:01"),
	(2, 3, "2010-01-01 00:00:01"),
	(2, 4, "2010-01-01 00:00:01"),
	(2, 5, "2010-01-01 00:00:01"),
	(2, 6, "2010-01-01 00:00:01"),
	(2, 7, "2010-01-01 00:00:01"),
	(3, 2, "2010-01-01 00:00:01"),
	(3, 5, "2010-01-01 00:00:01"),
	(3, 7, "2010-01-01 00:00:01"),
	(6, 7, "2010-01-01 00:00:01"),
	(7, 1, "2010-01-01 00:00:01");
