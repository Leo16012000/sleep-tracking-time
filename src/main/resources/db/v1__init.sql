-- PGADMIN 4 - Tuan Anh
CREATE TYPE "Role" AS ENUM (
  'ADMIN',
  'USER',
  'COLLABORATOR'
);

CREATE TYPE "QuestionType" AS ENUM (
  'SELECT_MULTIPLE',
  'SELECT_ONE'
);

CREATE TABLE "users" (
                         "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                         "phone_number" VARCHAR,
                         "email" VARCHAR,
                         "avatar" VARCHAR,
                         "username" VARCHAR UNIQUE NOT NULL
);

CREATE TABLE "logins" (
                          "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                          "role" "Role" NOT NULL DEFAULT 'USER',
                          "username" VARCHAR NOT NULL,
                          "password" VARCHAR NOT NULL,
                          "user_id" UUID NOT NULL
);

CREATE TABLE "refresh_tokens" (
                                  "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                                  "refresh_token" VARCHAR UNIQUE NOT NULL,
                                  "user_id" UUID,
                                  "expired_at" timestamp NOT NULL
);

CREATE TABLE "hashtags" (
                            "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                            "name" VARCHAR UNIQUE NOT NULL
);

CREATE TABLE "questions" (
                             "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                             "question" text NOT NULL,
                             "correct_answers" text NOT NULL,
                             "created_by_id" UUID,
                             "explanation" text,
                             "question_type" "QuestionType"
);

CREATE TABLE "quizs" (
                         "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                         "name" VARCHAR NOT NULL,
                         "description" VARCHAR NOT NULL,
                         "created_by_id" UUID NOT NULL,
                         "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         "updated_at" timestamp DEFAULT CURRENT_TIMESTAMP,
                         "deleted_at" timestamp
);

CREATE TABLE "quiz_question_associations" (
                                              "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                                              "quiz_id" UUID,
                                              "question_id" UUID NOT NULL,
                                              "order" INTEGER NOT NULL
);

CREATE TABLE "quiz_hashtag_associations" (
                                             "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                                             "quiz_id" UUID,
                                             "hashtag_id" UUID
);

CREATE TABLE "topic_hashtag_associations" (
                                              "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                                              "topic_id" UUID,
                                              "hashtag_id" UUID
);

CREATE TABLE "topics" (
                          "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                          "name" VARCHAR NOT NULL
);

CREATE TABLE "quiz_topic_associations" (
                                           "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                                           "quiz_id" UUID,
                                           "topic_id" UUID,
                                           "order" INTEGER NOT NULL
);

CREATE TABLE "sessions" (
                            "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
                            "total_questions" INTEGER,
                            "correct_questions" INTEGER,
                            "result_detail" text,
                            "created_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            "quiz_id" UUID
);

CREATE TABLE "settings" (
    "id" UUID PRIMARY KEY DEFAULT (gen_random_uuid())
);

ALTER TABLE "logins" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "refresh_tokens" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "questions" ADD FOREIGN KEY ("created_by_id") REFERENCES "users" ("id");

ALTER TABLE "quizs" ADD FOREIGN KEY ("created_by_id") REFERENCES "users" ("id");

ALTER TABLE "quiz_question_associations" ADD FOREIGN KEY ("quiz_id") REFERENCES "quizs" ("id");

ALTER TABLE "quiz_question_associations" ADD FOREIGN KEY ("question_id") REFERENCES "questions" ("id");

ALTER TABLE "quiz_question_associations" ADD FOREIGN KEY ("question_id") REFERENCES "hashtags" ("id");

ALTER TABLE "quiz_hashtag_associations" ADD FOREIGN KEY ("quiz_id") REFERENCES "quizs" ("id");

ALTER TABLE "quiz_hashtag_associations" ADD FOREIGN KEY ("hashtag_id") REFERENCES "hashtags" ("id");

ALTER TABLE "topic_hashtag_associations" ADD FOREIGN KEY ("topic_id") REFERENCES "topics" ("id");

ALTER TABLE "topic_hashtag_associations" ADD FOREIGN KEY ("hashtag_id") REFERENCES "hashtags" ("id");

ALTER TABLE "quiz_topic_associations" ADD FOREIGN KEY ("quiz_id") REFERENCES "quizs" ("id");

ALTER TABLE "quiz_topic_associations" ADD FOREIGN KEY ("topic_id") REFERENCES "topics" ("id");

ALTER TABLE "sessions" ADD FOREIGN KEY ("quiz_id") REFERENCES "quizs" ("id");