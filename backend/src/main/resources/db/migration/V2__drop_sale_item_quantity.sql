-- Each sale line is now a single item: the cashier enters one price per line and
-- there is no quantity input, so sale_items.quantity is no longer part of the model.
--
-- Historical rows keep their recorded subtotal (unit_price x quantity at the time of
-- the sale) and their sale totals, so past sales stay financially correct. What is
-- lost is the per-line item count for those older rows; unit_price alone no longer
-- reconstructs the subtotal where quantity was greater than 1.
alter table sale_items drop column quantity;
