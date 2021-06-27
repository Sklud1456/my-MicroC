// micro-C example 8 -- loop 20 million times

void main() {
	int i;
	for (i = 1; i <= 10; i++) {
		if (i % 2 == 0) {
			continue;
		}
		print i;
	}
	print 'S';
	for (i = 1; i <= 10; i++) {
		if (i == 6) {
			break;
		}
		print i;
	}
}
