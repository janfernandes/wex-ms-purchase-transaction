create table t_purchase_transaction
(
    purchase_transaction_id bigint         not null auto_increment,
    description             varchar(50)    not null,
    transaction_date        date           not null,
    purchase_amount         decimal(10, 2) not null,

    primary key (purchase_transaction_id)
);

insert into t_purchase_transaction (description, transaction_date, purchase_amount)
values ('Jones Markets', DATE '2024-02-29', 99.45)
