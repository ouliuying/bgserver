
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

INSERT INTO public.base_corp(address, comment, fax, id, name, telephone, website) VALUES ('上海杨浦区平凉路1398号', '上海星野信息科技有限公司，主要从事电信系统的集成与开发服务', '021-33772385', 1, '上海星野信息科技有限公司', '021-33772385', 'https://bg.work');



INSERT INTO public.base_partner_role(ac_rule, corp_id, id, is_super, name, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES ('', 1, 1, 1, '超级管理员', 1, 1, '2010-10-11 10:10:10', 1, 1, '2010-10-11 10:10:10');




SELECT pg_catalog.setval('public.base_app_id_seq', 1, true);


SELECT pg_catalog.setval('public.base_corp_id_seq', 5, true);




INSERT INTO public.base_partner(chat_uuid, access_token_key, birthday, email, id, mobile, name, nick_name, password, sync_tag, tag, telephone, user_comment, user_icon, user_name, user_title, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES ('sncodesn', 'sncode', '2018-10-10', 'bayoujishu001@qq.com', 1, '18621991588', '管理员', '管理员', '21232f297a57a5a743894a0e4a801fc3', '1', 1, '021-33772385', '星野技术', '0f739f5f3bc243d9a10108f3c38640d2', 'admin', '星野技术', 1, NULL, '2019-10-13 10:10:10', 1, NULL, '2019-10-13 10:10:10');

INSERT INTO public.base_partner(chat_uuid, access_token_key, birthday, email, id, mobile, name, nick_name, password, sync_tag, tag, telephone, user_comment, user_icon, user_name, user_title, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES ('f73bb9b5-77b2-457e-8942-9113140e9d0b', NULL, NULL, 'bg.work', 7, 'bg.work', 'admin2', 'admin2', 'c84258e9c39059a89ab77d846ddab909', NULL, 2, 'bg.work', 'bg.work', '7519f8ae6ec24630a61db1e4c41bd699', 'admin2', 'bg.work', 1, 1, '2019-10-24 11:18:46.184', 1, 1, '2019-10-24 11:18:46.184');

SELECT pg_catalog.setval('public.base_partner_id_seq', 7, true);


INSERT INTO public.base_corp_partner_rel(corp_id, id, is_default_corp, partner_id, partner_role_id) VALUES (1, 2, 1, 1, 1);
INSERT INTO public.base_corp_partner_rel(corp_id, id, is_default_corp, partner_id, partner_role_id) VALUES (1, 3, 1, 7, 1);



SELECT pg_catalog.setval('public.base_corp_partner_rel_id_seq', 3, true);





INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 2, 0, 1, 1, 1, '2019-10-24 11:13:27.628', 1, 1, '2019-10-24 11:13:27.628');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 3, 1, 1, 1, 1, '2019-10-24 11:13:27.631', 1, 1, '2019-10-24 11:13:27.631');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 4, 2, 1, 1, 1, '2019-10-24 11:13:27.632', 1, 1, '2019-10-24 11:13:27.632');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 5, 3, 1, 1, 1, '2019-10-24 11:13:27.633', 1, 1, '2019-10-24 11:13:27.633');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 6, 4, 1, 1, 1, '2019-10-24 11:13:27.634', 1, 1, '2019-10-24 11:13:27.634');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 7, 5, 1, 1, 1, '2019-10-24 11:13:27.636', 1, 1, '2019-10-24 11:13:27.636');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 8, 6, 1, 1, 1, '2019-10-24 11:13:27.636', 1, 1, '2019-10-24 11:13:27.636');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 9, 7, 1, 1, 1, '2019-10-24 11:13:27.637', 1, 1, '2019-10-24 11:13:27.637');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 10, 0, 7, 1, 7, '2019-10-24 11:20:21.075', 1, 7, '2019-10-24 11:20:21.075');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 11, 1, 7, 1, 7, '2019-10-24 11:20:21.076', 1, 7, '2019-10-24 11:20:21.076');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 12, 2, 7, 1, 7, '2019-10-24 11:20:21.076', 1, 7, '2019-10-24 11:20:21.076');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 13, 3, 7, 1, 7, '2019-10-24 11:20:21.077', 1, 7, '2019-10-24 11:20:21.077');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 14, 4, 7, 1, 7, '2019-10-24 11:20:21.077', 1, 7, '2019-10-24 11:20:21.077');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 15, 5, 7, 1, 7, '2019-10-24 11:20:21.078', 1, 7, '2019-10-24 11:20:21.078');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 16, 6, 7, 1, 7, '2019-10-24 11:20:21.078', 1, 7, '2019-10-24 11:20:21.078');
INSERT INTO public.base_partner_app_shortcut(app_id, id, shortcut_index, partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 17, 7, 7, 1, 7, '2019-10-24 11:20:21.079', 1, 7, '2019-10-24 11:20:21.079');




SELECT pg_catalog.setval('public.base_partner_app_shortcut_id_seq', 17, true);









SELECT pg_catalog.setval('public.base_partner_role_id_seq', 1, true);



--

INSERT INTO public.base_storage_entity(client_name, id, is_transient, request_name, server_path, typ, typ_title, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES ('无标题LOGO2.png', 1, 0, '0f739f5f3bc243d9a10108f3c38640d2', 'static\upload\path\fb3acc15-6f2c-451c-b55d-0c941edcccdc.png', 'image', '图片', 1, 1, '2019-10-24 11:15:52.556', 1, 1, '2019-10-24 11:15:52.556');
INSERT INTO public.base_storage_entity(client_name, id, is_transient, request_name, server_path, typ, typ_title, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES ('QQ图片20190326185436.jpg', 2, 0, '7519f8ae6ec24630a61db1e4c41bd699', 'static\upload\path\627cad98-f128-45e5-81fd-982988837327.jpg', 'image', '图片', 1, 1, '2019-10-24 11:18:12.643', 1, 1, '2019-10-24 11:18:12.643');





INSERT INTO public.base_partner_storage_entity_rel(id, owner_typ, partner_id, storage_entity_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (1, 0, 1, 1, 1, 1, '2019-10-24 11:15:52.561', 1, 1, '2019-10-24 11:15:52.561');
INSERT INTO public.base_partner_storage_entity_rel(id, owner_typ, partner_id, storage_entity_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (2, 0, 1, 2, 1, 1, '2019-10-24 11:18:12.645', 1, 1, '2019-10-24 11:18:12.645');





SELECT pg_catalog.setval('public.base_partner_storage_entity_rel_id_seq', 2, true);



SELECT pg_catalog.setval('public.base_storage_entity_id_seq', 2, true);



INSERT INTO public.chat_channel(broadcast_type, default_flag, icon, id, name, partner_id, uuid, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (0, 1, NULL, 1, '全体成员', 1, 'e7edaff7-cac2-45c4-a778-36cd83877491', 1, 1, '2019-10-24 11:14:09.902', 1, 1, '2019-10-24 11:14:09.902');
INSERT INTO public.chat_channel(broadcast_type, default_flag, icon, id, name, partner_id, uuid, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (1, 0, NULL, 2, '我的部门', 1, '7a8d9f7c-3fe9-4401-b128-4e5830c1dae0', 1, 1, '2019-10-24 11:14:38.844', 1, 1, '2019-10-24 11:14:38.844');

SELECT pg_catalog.setval('public.chat_channel_id_seq', 2, true);


INSERT INTO public.chat_model_join_channel_rel(id, join_channel_id, join_partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (1, 2, 7, 1, 7, '2019-10-24 11:20:46.219', 1, 7, '2019-10-24 11:20:46.219');
INSERT INTO public.chat_model_join_channel_rel(id, join_channel_id, join_partner_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (2, 1, 7, 1, 7, '2019-10-24 11:20:50.69', 1, 7, '2019-10-24 11:20:50.69');


SELECT pg_catalog.setval('public.chat_model_join_channel_rel_id_seq', 2, true);




INSERT INTO public.corp_department(comment, corp_id, id, name, parent_id, create_corp_id, create_partner_id, create_time, last_modify_corp_id, last_modify_partner_id, last_modify_time) VALUES (NULL, 1, 2, '技术部', NULL, 1, 1, '2019-10-24 11:17:48.673', 1, 1, '2019-10-24 11:17:48.673');

SELECT pg_catalog.setval('public.corp_department_id_seq', 2, true);
