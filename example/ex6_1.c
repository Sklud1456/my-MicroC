// micro-C example 6 -- return a result via a pointer argument; nested blocks

void main(int n) {
	switch (n){
		
		case 1: {
			print 'a';
			break;
		}
		case 2: {
			print 'b';
			
		}
		case 3: {
			print 'c';
			break;
		}
		default: {
			print 's';
			break;
		}
	}
}

