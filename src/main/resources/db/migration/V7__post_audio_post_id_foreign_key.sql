DELETE FROM post_audio
WHERE post_id NOT IN (SELECT id FROM post);

ALTER TABLE post_audio
    ADD CONSTRAINT fk_post_audio_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE;
