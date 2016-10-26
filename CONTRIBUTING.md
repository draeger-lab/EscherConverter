We love contributions from everyone.
Great to have you here.
Here are a few ways you can help make this project better!


## Learn & listen

- Please open an issue if you face any problem while using or developing this project. We keep a keen eye on the issues and pull request.
- You can also email the maintainers of this project: @draeger and @devkhan. E-Mail Ids can be found on their profile.


## Points to remember

* Please go through the issues first to see if your problem is already listed or a feature request has already been made.
* Please talk to one of maintainers before starting to work on a feature of bug fixes.
* Please make all tests are passing and CI build is succeeding while submitting a PR. If you need help, contact the maintainers.
* Please write useful and concise commit messages.
* Please be patient and polite.


## Documentation

Currently only JavaDocs exist for the core Java executable. You can help by contributing usage guides, concept explanations, etc. Also, keep in mind to document whatever code you contributing.


## Contributing Code


1. Fork the repo and clone it using git client of your choice.
2. There are three parts of this project to which you can contribute. All three of them are included in the IDE files in the `.idea` folders. You can use whatever IDE/editor you want but we prefer InteliiJ IDEA as it requires the least setup, just open the project and build.

##### Executable JAR (Java)

If working on the command-line, you need maven to build this JAR. Just use the following maven goals:

    mvn clean compile assembly:single test
    
Now try running the JAR either from the command-line or by double-clicking on it.

##### REST API (Python + Flask)

The REST API is a simple Flask app, so can be run in any way you like, simplest being(in the `api` directory):

    python app.py

##### Front-end (Node)

The front-end is built using `brunch`, so you need to install that first:

    npm i -g brunch
    
Now to build the front-end, just run(in `frontend` directory):

    npm run build

Now, to test the front-end(in the generated `public` directory):

    python2 -m SimpleHTTPServer <any port number>

OR

    python3 -m http.server <any port number>
    
For any further queries, contact [Devesh Khandelwal](@devkhan).


Others will give constructive feedback.
This is a time for discussion and improvements,
and making the necessary changes will be required before we can
merge the contribution.
