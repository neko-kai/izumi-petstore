import { HelloWorld } from './generated/petstore/api'

function main() {
    const hello = new HelloWorld({'hello': 'Hello'});

    console.log(hello.getFullClassName());
    console.log(hello.hello);
}

main();
