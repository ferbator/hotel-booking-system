-- Hotels
INSERT INTO hotel (id, name, address)
VALUES
    (1, 'Test1', 'Address1'),
    (2, 'Test2', 'Address2');

-- Rooms
INSERT INTO room (id, hotel_id, number, available)
VALUES
    (1, 1, '1', true),
    (2, 1, '2', true),
    (3, 2, '3', true),
    (4, 2, '4', true);
