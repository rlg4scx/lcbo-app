# lcbo-app

Sample app for SCX which vends a random Beer each week, read out of the LCBO api. There shouldn't be any duplicates appearing in the beer of the week.

There's also a random beer page which vends a random beer but might include duplicates.

## Check out and run the code

To check out the code:

    $ git clone https://github.com/rlg4scx/lcbo-app.git

To build and run the code, open the Play console:

    $ cd lcbo-app
    $ ./activator
    # more info on the Play console: https://www.playframework.com/documentation/2.3.x/PlayConsole

... and run the application.

    [play-java-2.4] run
    
It should launch (in dev mode) on port 9000:

    http://localhost:9000/

... although when you hit that page you may be prompted to run some evolutions: https://www.playframework.com/documentation/2.0/Evolutions
    
## What the hell does this do?

For every week, the app chooses a random beer (which isn't a duplicate from past weeks) to be the "Beer of the Week". Under the hood, a chron-like process (Akka) asynchonously pings the LCBO API for product information and dumps it into a database. (An EBean Database - the Play Framework default.)

It also offers a page which outputs a random Beer which might be a duplicate.
