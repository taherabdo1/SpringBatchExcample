create table if not exists `log_record` (
	`id` BIGINT unsigned not null  auto_increment,
	`date` date not null,
	`ip` varchar(30),
	`request` varchar(50) not null,
	`status` int not null,
	`agent` varchar(200) not null,
	PRIMARY KEY(`id`)
)AUTO_INCREMENT=1;