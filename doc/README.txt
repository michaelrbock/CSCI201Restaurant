v4.2:
-------
- All data that needs to be is now synchronized
- Waiters are randomly (with a 50% chance) either normal waiters or shared-data waiters (i.e. use the revolving stand for orders)
    - Waiters that use shared-data are marked as such in their name (e.g. "w2(shared data)")
    - You can see those waiters place orders onto the stand and the cook take them off
- Run restaurant.test.FullCashierTest as a JUnit Test to run through the Cashier's tests
- You can see that multi-step ordering has been implemented between the waiters and customers


v4.1:
--------
Scenarios mostly run by themselves (randomly).

For instance:
- If there is a wait for the customer: the customer has a 50/50 chance of waiting or leaving.
- The customer starts with a random amount of money between 0 and 30 dollars.
    - Therefore when the customer pays, there is a chance he will be under and will have to work.
    - Customer works for 1000ms for each 8 dollars under, calculated by how much under the cost he is.
- Cook will run out of food after too many of that type have been ordered (starts with 2 of each only and 0 steaks).
	- You can see he orders more steaks at the very beginning
    - He will then order from the first market on his list.
    - The market runs out in the same way as the cook -- after too many are ordered.
    - The cook then tries the next market (or a random one if they were all out last time he checked).

Exceptions:
- Press the "On Break?" button on the GUI for the waiter to see break scenario between waiter and host.

