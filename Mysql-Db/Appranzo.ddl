CREATE DATABASE ApPranzo;

USE ApPranzo;

CREATE TABLE CATEGORIES (
    id INT AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE CITIES (
    id INT AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    region VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);


CREATE TABLE PLACES (
    id INT AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    city_id INT NOT NULL,
    address VARCHAR(255),
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    photo_url VARCHAR(255),
    category_id INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES CATEGORIES(id),
    FOREIGN KEY (city_id) REFERENCES CITIES(id)
);

CREATE TABLE USERS (
    id INT AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    points INT NOT NULL DEFAULT 0,
    photo_url VARCHAR(255),
    hashed_refresh_token VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE REVIEWS (
    id INT AUTO_INCREMENT,
    place_id INT NOT NULL,
    user_id INT NOT NULL,
    rating TINYINT NOT NULL,
    price_level TINYINT CHECK (price_level BETWEEN 1 AND 5),
    ambience_rating TINYINT CHECK (ambience_rating BETWEEN 1 AND 5),
    ingredient_quality TINYINT CHECK (ingredient_quality BETWEEN 1 AND 5),
    comment TEXT,
    creation_date DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (place_id) REFERENCES PLACES(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE
);

CREATE TABLE FAVORITES (
    id INT AUTO_INCREMENT,
    place_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UNICITA_FAV UNIQUE (place_id, user_id),
    FOREIGN KEY (place_id) REFERENCES PLACES(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE
);


CREATE TABLE CLIENT_PHOTOS (
    id INT AUTO_INCREMENT,
    photo_url VARCHAR(255) NOT NULL,
    review_id INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (review_id) REFERENCES REVIEWS(id) ON DELETE CASCADE
);


CREATE TABLE FRIENDSHIP_REQUEST (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (sender_id) REFERENCES USERS(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES USERS(id) ON DELETE CASCADE
);

CREATE TABLE FRIENDSHIPS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id_1 INT NOT NULL,
    user_id_2 INT NOT NULL,
    request_id INT,
    friendship_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id_1) REFERENCES USERS(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id_2) REFERENCES USERS(id) ON DELETE CASCADE,
    FOREIGN KEY (request_id) REFERENCES FRIENDSHIP_REQUEST(id) ON DELETE SET NULL,
    CONSTRAINT Only_One_Friendship_For_Couple UNIQUE (user_id_1, user_id_2)
);



DELIMITER $$

CREATE TRIGGER friendships_order_trigger
BEFORE INSERT ON FRIENDSHIPS
FOR EACH ROW
BEGIN
    DECLARE tmp INT;

    IF NEW.user_id_1 > NEW.user_id_2 THEN
        SET tmp = NEW.user_id_1;
        SET NEW.user_id_1 = NEW.user_id_2;
        SET NEW.user_id_2 = tmp;
    END IF;
    
END$$
DELIMITER ;

DELIMITER $$

CREATE TRIGGER trigger_creates_friendship_after_accept
AFTER UPDATE ON FRIENDSHIP_REQUEST
FOR EACH ROW
BEGIN
    IF NEW.status = 'accepted' THEN
        INSERT INTO FRIENDSHIPS (user_id_1, user_id_2, request_id)
        VALUES (NEW.sender_id, NEW.receiver_id, NEW.id);
    END IF;
END$$

DELIMITER ;

