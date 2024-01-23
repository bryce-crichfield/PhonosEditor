package piano.util.tuple;

public class Tuple3<A, B, C> implements Tuple {
    public A _1;
    public B _2;
    public C _3;

    public Tuple3(A a, B b, C c) {
        this._1 = a;
        this._2 = b;
        this._3 = c;
    }

    public String toString() {
        return "(" + _1 + ", " + _2 + ", " + _3 + ")";
    }

    public boolean equals(Object o) {
        if (!(o instanceof Tuple3)) {
            return false;
        }
        Tuple3 t = (Tuple3) o;
        return _1.equals(t._1) && _2.equals(t._2) && _3.equals(t._3);
    }

    public int hashCode() {
        return _1.hashCode() ^ _2.hashCode() ^ _3.hashCode();
    }
}
