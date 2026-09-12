ALTER TABLE tag_content DROP COLUMN description;

CREATE TABLE project_tag (
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, tag_id)
);

CREATE INDEX idx_project_tag_project_id ON project_tag (project_id);
CREATE INDEX idx_project_tag_tag_id ON project_tag (tag_id);

DO $$
DECLARE
    proj RECORD;
    lang TEXT;
    tag_slug TEXT;
    tag_id UUID;
BEGIN
    FOR proj IN SELECT id, programming_language::text as prog_lang FROM project WHERE programming_language IS NOT NULL AND programming_language != ''
    LOOP
        FOREACH lang IN ARRAY string_to_array(proj.prog_lang, ',')
        LOOP
            lang := trim(lang);
            IF lang != '' THEN
                tag_slug := lower(regexp_replace(regexp_replace(lang::text, '[^a-zA-Z0-9]+', '-', 'g'), '^-|-$', '', 'g'));

                SELECT id INTO tag_id FROM tag WHERE slug = tag_slug;

                IF tag_id IS NULL THEN
                    INSERT INTO tag (slug) VALUES (tag_slug) RETURNING id INTO tag_id;
                    INSERT INTO tag_content (tag_id, language, name) VALUES (tag_id, 'ENGLISH', lang);
                END IF;

                INSERT INTO project_tag (project_id, tag_id) VALUES (proj.id, tag_id) ON CONFLICT DO NOTHING;
            END IF;
        END LOOP;
    END LOOP;
END $$;

ALTER TABLE project DROP COLUMN programming_language;
