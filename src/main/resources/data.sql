DROP TABLE IF EXISTS article;

CREATE TABLE article (
                         id INT AUTO_INCREMENT  PRIMARY KEY,
                         url VARCHAR(255) NOT NULL,
                         title VARCHAR(255) NOT NULL,
                         rating VARCHAR(255) NOT NULL,
                         content CLOB NOT NULL
);

INSERT INTO article (url, title, rating, content) VALUES
  (
  'https://www.aktuality.sk/clanok/851842/od-1-januara-nastanu-zmeny-v-zasielani-postovych-zasielok-do-krajin-mimo-eu/',
  'Od 1. januára nastanú zmeny v zasielaní poštových zásielok do krajín mimo EÚ | Aktuality.sk',
  'true',
  'CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT CONTENT'
  );