int add(int x, int y) {
    int z;
    z = x + y;
    return z;
}

void main() {
    int t = 33;
    while (t < 35) {
        t = t + 1;
        _print(add(1, t));
    }
}