import { HelloWorld } from 'generated/petstore/api'

function main() {
    const hello = new HelloWorld({'hello': 'Hello'});

    console.log(hello.toString());
}

main();
