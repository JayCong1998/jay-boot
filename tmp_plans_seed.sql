INSERT INTO plans (id,code,name,billing_cycle,quota_json,price,status,creator_id,creator_name,created_time,updater_id,updater_name,updated_time,is_deleted)
VALUES
(1949000000000001001,'STARTER_MONTHLY','Starter Plan (Monthly)','MONTHLY','{"monthlyGenerations":200,"seats":1,"advancedTemplates":false,"batchExport":false,"teamCollaboration":false,"highlights":["200 generations per month","Basic templates","Good for solo beginners"]}',4900,'ACTIVE',1,'system',NOW(3),1,'system',NOW(3),0),
(1949000000000001002,'PRO_MONTHLY','Pro Plan (Monthly)','MONTHLY','{"monthlyGenerations":1200,"seats":1,"advancedTemplates":true,"batchExport":true,"teamCollaboration":false,"highlights":["1200 generations per month","Advanced templates","Priority support"]}',12900,'ACTIVE',1,'system',NOW(3),1,'system',NOW(3),0),
(1949000000000001003,'TEAM_MONTHLY','Team Plan (Monthly)','MONTHLY','{"monthlyGenerations":4000,"seats":10,"advancedTemplates":true,"batchExport":true,"teamCollaboration":true,"highlights":["4000 generations per month","Team collaboration","Dedicated success support"]}',26900,'ACTIVE',1,'system',NOW(3),1,'system',NOW(3),0),
(1949000000000001004,'STARTER_YEARLY','Starter Plan (Yearly)','YEARLY','{"monthlyGenerations":200,"seats":1,"advancedTemplates":false,"batchExport":false,"teamCollaboration":false,"highlights":["200 generations per month","Basic templates","Lower annual pricing"]}',3900,'ACTIVE',1,'system',NOW(3),1,'system',NOW(3),0),
(1949000000000001005,'PRO_YEARLY','Pro Plan (Yearly)','YEARLY','{"monthlyGenerations":1200,"seats":1,"advancedTemplates":true,"batchExport":true,"teamCollaboration":false,"highlights":["1200 generations per month","Advanced templates","Best value yearly"]}',9900,'ACTIVE',1,'system',NOW(3),1,'system',NOW(3),0),
(1949000000000001006,'TEAM_YEARLY','Team Plan (Yearly)','YEARLY','{"monthlyGenerations":4000,"seats":10,"advancedTemplates":true,"batchExport":true,"teamCollaboration":true,"highlights":["4000 generations per month","Team collaboration","Dedicated success support"]}',21900,'ACTIVE',1,'system',NOW(3),1,'system',NOW(3),0)
ON DUPLICATE KEY UPDATE
name=VALUES(name),
billing_cycle=VALUES(billing_cycle),
quota_json=VALUES(quota_json),
price=VALUES(price),
status=VALUES(status),
updater_id=VALUES(updater_id),
updater_name=VALUES(updater_name),
updated_time=NOW(3),
is_deleted=VALUES(is_deleted);

SELECT id,code,name,billing_cycle,price,status,JSON_EXTRACT(quota_json, '$.highlights[0]') AS highlight0
FROM plans
ORDER BY billing_cycle,price;
