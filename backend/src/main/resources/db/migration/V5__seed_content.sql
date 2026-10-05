-- Draft A1 content seed. LANGUAGE CONTENT IS A DRAFT:
-- every lesson_item is inserted with needs_review = true and must be reviewed by a native speaker.
-- Topics follow the product spec (greetings, introductions, family, numbers, colors, food, ...).

INSERT INTO courses (code, title, level)
VALUES ('BURYAT_A1', 'Буряад хэлэн — A1', 'BEGINNER');

DO $$
DECLARE
    v_course_id BIGINT;
    v_lesson_id BIGINT;
BEGIN
    SELECT id INTO v_course_id FROM courses WHERE code = 'BURYAT_A1';

    -- 1. Greetings
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Сайн байна!', 'Приветствия', 1, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'PHRASE', 'Поздоровайтесь', 'Приветствие', 'Сайн байна!', 'Здравствуйте! / Привет!', NULL, NULL, NULL, 'Универсальное приветствие.', true),
        (v_lesson_id, 2, 'PHRASE', 'Попрощайтесь', 'Прощание', 'Баяртай!', 'До свидания!', NULL, NULL, NULL, 'Используется при прощании.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «Сайн байна!»?', 'Что означает приветствие?', NULL, NULL, '["Спасибо","Здравствуйте","До свидания"]'::jsonb, 1, 'Здравствуйте', '«Сайн байна» — буквально «хорошо есть», приветствие.', true),
        (v_lesson_id, 4, 'MULTIPLE_CHOICE', 'Как сказать «До свидания»?', 'Выберите прощание', NULL, NULL, '["Баяртай","Сайн байна","hайн даа"]'::jsonb, 0, 'Баяртай', '«Баяртай» — до свидания.', true);

    -- 2. Introducing yourself
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Танилсая!', 'Как тебя зовут?', 2, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'PHRASE', 'Спросите имя', 'Знакомство', 'Танилсая!', 'Давай познакомимся!', NULL, NULL, NULL, 'Форма совместного действия.', true),
        (v_lesson_id, 2, 'PHRASE', 'Спросите «Как тебя зовут?»', 'Вопрос об имени', 'Ши нэрэ хэн гэжэ нэрэтэйб?', 'Как тебя зовут?', NULL, NULL, NULL, 'Черновой вариант формулировки.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «Танилсая!»?', 'Перевод фразы', NULL, NULL, '["Давай познакомимся","До свидания","Спасибо"]'::jsonb, 0, 'Давай познакомимся', 'Побудительная форма.', true),
        (v_lesson_id, 4, 'TRANSLATION', 'Выберите перевод: «Как тебя зовут?»', 'Перевод вопроса', NULL, NULL, '["Ши нэрэ хэн гэжэ нэрэтэйб?","Сайн байна!","Баяртай!"]'::jsonb, 0, 'Ши нэрэ хэн гэжэ нэрэтэйб?', 'Черновой вариант, требует проверки.', true);

    -- 3. My name is
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Минии нэрэ...', 'Меня зовут...', 3, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'PHRASE', 'Скажите, как вас зовут', 'Представление', 'Минии нэрэ Баяр.', 'Меня зовут Баяр.', NULL, NULL, NULL, 'Минии — мой/моя.', true),
        (v_lesson_id, 2, 'WORD', 'Слово «имя»', 'Словарь', 'нэрэ', 'имя', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «Минии нэрэ...»?', 'Перевод', NULL, NULL, '["Меня зовут...","Я иду...","Я вижу..."]'::jsonb, 0, 'Меня зовут...', 'Минии — притяжательное.', true);

    -- 4. Where are you from
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Хаанаhаа ерэбэш?', 'Откуда ты?', 4, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'PHRASE', 'Спросите, откуда собеседник', 'Вопрос о происхождении', 'Хаанаhаа ерэбэш?', 'Откуда ты приехал(а)?', NULL, NULL, NULL, 'Черновая форма.', true),
        (v_lesson_id, 2, 'PHRASE', 'Ответьте «Я из Улан-Удэ»', 'Ответ о происхождении', 'Би Улаан-Удэhээ ерэбэ.', 'Я из Улан-Удэ.', NULL, NULL, NULL, 'Топоним дан как пример.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «Хаанаhаа»?', 'Перевод', NULL, NULL, '["Откуда","Куда","Когда"]'::jsonb, 0, 'Откуда', 'Вопросительное слово.', true);

    -- 5. I study Buryat
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Би буряад хэлэ үзэнэб', 'Я изучаю бурятский', 5, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'PHRASE', 'Скажите, что учите бурятский', 'О языке', 'Би буряад хэлэ үзэнэб.', 'Я изучаю бурятский язык.', NULL, NULL, NULL, 'Черновая форма.', true),
        (v_lesson_id, 2, 'WORD', 'Слово «язык»', 'Словарь', 'хэлэн', 'язык', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «хэлэн»?', 'Перевод', NULL, NULL, '["язык","город","дом"]'::jsonb, 0, 'язык', 'Базовая лексика.', true);

    -- 6. Family
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Минии гэр бүлэ', 'Моя семья', 6, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'WORD', 'Слово «мама»', 'Словарь', 'эжы', 'мама', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 2, 'WORD', 'Слово «папа»', 'Словарь', 'аба', 'папа', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «эжы»?', 'Перевод', NULL, NULL, '["мама","сестра","бабушка"]'::jsonb, 0, 'мама', 'Базовая лексика.', true),
        (v_lesson_id, 4, 'TRANSLATION', 'Выберите перевод: «папа»', 'Перевод', NULL, NULL, '["аба","эжы","ахы"]'::jsonb, 0, 'аба', 'Базовая лексика.', true);

    -- 7. Numbers
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Тоонууд', 'Числа 1–5', 7, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'WORD', 'Слово «один»', 'Словарь', 'нэгэн', 'один', NULL, NULL, NULL, 'Черновая форма.', true),
        (v_lesson_id, 2, 'WORD', 'Слово «два»', 'Словарь', 'хоёр', 'два', NULL, NULL, NULL, 'Черновая форма.', true),
        (v_lesson_id, 3, 'WORD', 'Слово «три»', 'Словарь', 'гурбан', 'три', NULL, NULL, NULL, 'Черновая форма.', true),
        (v_lesson_id, 4, 'MULTIPLE_CHOICE', 'Что означает «хоёр»?', 'Перевод', NULL, NULL, '["один","два","три"]'::jsonb, 1, 'два', 'Числительное.', true);

    -- 8. Colors
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Үнгүүд', 'Цвета', 8, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'WORD', 'Слово «белый»', 'Словарь', 'сагаан', 'белый', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 2, 'WORD', 'Слово «чёрный»', 'Словарь', 'хара', 'чёрный', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «сагаан»?', 'Перевод', NULL, NULL, '["белый","красный","синий"]'::jsonb, 0, 'белый', 'Цвет.', true);

    -- 9. Food
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Эдеэн', 'Еда и напитки', 9, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'WORD', 'Слово «вода»', 'Словарь', 'уhан', 'вода', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 2, 'WORD', 'Слово «чай»', 'Словарь', 'сай', 'чай', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «уhан»?', 'Перевод', NULL, NULL, '["вода","хлеб","молоко"]'::jsonb, 0, 'вода', 'Базовая лексика.', true),
        (v_lesson_id, 4, 'TRANSLATION', 'Выберите перевод: «чай»', 'Перевод', NULL, NULL, '["сай","уhан","эдеэн"]'::jsonb, 0, 'сай', 'Базовая лексика.', true);

    -- 10. Days / time
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Үдэрнүүд', 'Дни недели', 10, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'WORD', 'Слово «день»', 'Словарь', 'үдэр', 'день', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 2, 'WORD', 'Слово «сегодня»', 'Словарь', 'мүнөөдэр', 'сегодня', NULL, NULL, NULL, 'Черновая форма.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «мүнөөдэр»?', 'Перевод', NULL, NULL, '["сегодня","завтра","вчера"]'::jsonb, 0, 'сегодня', 'Наречие времени.', true);

    -- 11. City
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Хото', 'Город и места', 11, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'WORD', 'Слово «город»', 'Словарь', 'хото', 'город', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 2, 'WORD', 'Слово «дом»', 'Словарь', 'гэр', 'дом', NULL, NULL, NULL, 'Базовая лексика.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «гэр»?', 'Перевод', NULL, NULL, '["дом","дорога","река"]'::jsonb, 0, 'дом', 'Базовая лексика.', true);

    -- 12. Farewell
    INSERT INTO lessons (course_id, title, subtitle, order_index, level, xp_reward)
    VALUES (v_course_id, 'Баяртай!', 'Прощание и вежливость', 12, 'BEGINNER', 20)
    RETURNING id INTO v_lesson_id;
    INSERT INTO lesson_items (lesson_id, order_index, type, prompt, prompt_translation, buryat, russian, options, correct_option_index, correct_answer_text, explanation, needs_review) VALUES
        (v_lesson_id, 1, 'PHRASE', 'Попрощайтесь', 'Прощание', 'Баяртай!', 'До свидания!', NULL, NULL, NULL, 'Повторение.', true),
        (v_lesson_id, 2, 'PHRASE', 'Скажите «спасибо»', 'Вежливость', 'hайн даа', 'спасибо / хорошо', NULL, NULL, NULL, 'Черновой вариант, требует проверки.', true),
        (v_lesson_id, 3, 'MULTIPLE_CHOICE', 'Что означает «Баяртай»?', 'Перевод', NULL, NULL, '["До свидания","Привет","Спасибо"]'::jsonb, 0, 'До свидания', 'Прощание.', true);
END $$;