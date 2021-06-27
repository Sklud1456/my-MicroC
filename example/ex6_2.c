// micro-C example 6 -- return a result via a pointer argument; nested blocks

void main(int n) {
	switch (n){
		case 1: {
			printf("%c" ,'a');
		}
		case 2: {
			printf("%c", 'b');
		}
		case 3: {
			printf("%c", 'c');
		}
		default: {
			printf("%c", 'S');
		}
	}
}

