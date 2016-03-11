# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table beer_of_the_week (
  product_id                bigint not null,
  epoch_day                 bigint,
  name                      varchar(255),
  category                  varchar(255),
  producer_name             varchar(255),
  thumbnail                 varchar(255),
  constraint pk_beer_of_the_week primary key (product_id))
;

create sequence beer_of_the_week_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists beer_of_the_week;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists beer_of_the_week_seq;

