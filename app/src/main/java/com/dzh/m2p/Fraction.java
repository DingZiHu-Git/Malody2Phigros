package com.dzh.m2p;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;

public class Fraction implements Comparable<Fraction> {
	private final double EPSILON = 1e-6;
	private final int num;
	private final int up;
	private final int down;
	public Fraction() {
		this(0, 0, 1);
	}
	public Fraction(int num, int up, int down) {
		if (down == 0) throw new IllegalArgumentException("Denominator is zero");
		int[] formatted = format(num, up, down);
		this.num = formatted[0];
		this.up = formatted[1];
		this.down = formatted[2];
	}
	public Fraction(String expression) {
		String[] split1 = expression.split(":");
		int tempNum = 0;
		if (split1.length == 2) tempNum = Integer.parseInt(split1[0]);
		String[] split2 = split1[split1.length - 1].split("/");
		if (split2.length != 2) {
			num = tempNum;
			up = 0;
			down = 1;
			return;
		}
		int tempUp = Integer.parseInt(split2[0]);
		int tempDown = Integer.parseInt(split2[1]);
		if (tempDown == 0) throw new IllegalArgumentException("Denominator is zero");
		int[] formatted = format(tempNum, tempUp, tempDown);
		num = formatted[0];
		up = formatted[1];
		down = formatted[2];
	}
	public Fraction(JSONArray beat) throws JSONException {
		this(beat.getInt(0), beat.getInt(1), beat.getInt(2));
	}
	public Fraction(double d) {
		int tempNum = 0;
		int tempUp;
		int tempDown;
		boolean negative = false;
		if (d < 0) {
			d = -d;
			negative = true;
		}
		while (d >= 1) {
			d--;
			tempNum++;
		}
		for (int i = 1; ; i++) for (int j = 0; j < i; j++) {
			if (EPSILON >= Math.abs(j / (double) i - d)) {
				tempUp = j;
				tempDown = i;
				if (negative) tempNum = -tempNum;
				int[] formatted = format(tempNum, tempUp, tempDown);
				num = formatted[0];
				up = formatted[1];
				down = formatted[2];
				return;
			}
		}
	}
	public Fraction add(Fraction p) {
		int newNum = this.num + p.num;
		int newUp = this.up * p.down + p.up * this.down;
		int newDown = this.down * p.down;
		return new Fraction(newNum, newUp, newDown);
	}
	public Fraction subtract(Fraction p) {
		int newNum = this.num - p.num;
		int newUp = this.up * p.down - p.up * this.down;
		int newDown = this.down * p.down;
		return new Fraction(newNum, newUp, newDown);
	}
	public Fraction multiply(Fraction p) {
		int thisNumerator = this.up + this.num * this.down;
		int pNumerator = p.up + p.num * p.down;
		int newUp = thisNumerator * pNumerator;
		int newDown = this.down * p.down;
		return new Fraction(0, newUp, newDown);
	}
	public Fraction divide(Fraction p) {
		if (p.up == 0 && p.num == 0) throw new ArithmeticException("Divided by zero");
		int pNumerator = p.up + p.num * p.down;
		int newUp = (this.up + this.num * this.down) * p.down;
		int newDown = this.down * pNumerator;
		return new Fraction(0, newUp, newDown);
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Fraction fraction = (Fraction) o;
		return num == fraction.num && up == fraction.up && down == fraction.down;
	}
	@Override
	public int hashCode() {
		return Objects.hash(num, up, down);
	}
	@Override
	public int compareTo(Fraction other) {
		if (this.num != other.num) return Integer.compare(this.num, other.num);
		long thisNumerator = (long) this.up * other.down;
		long otherNumerator = (long) other.up * this.down;
		return Long.compare(thisNumerator, otherNumerator);
	}
	public BigDecimal toBigDecimal() {
		BigDecimal numPart = new BigDecimal(num);
		BigDecimal upBD = new BigDecimal(up);
		BigDecimal downBD = new BigDecimal(down);
		BigDecimal fractionPart = upBD.divide(downBD, 10, RoundingMode.HALF_UP);
		return numPart.add(fractionPart);
	}
	public int[] toIntArray() {
		return new int[]{ num, up, down };
	}
	public JSONArray toJSONArray() {
		return new JSONArray().put(num).put(up).put(down);
	}
	@Override
	public String toString() {
		return num + ":" + up + "/" + down;
	}
	public static int[] format(int num, int up, int down) {
		if (down == 0) throw new IllegalArgumentException("Denominator is zero");
		if (down < 0) {
			up = -up;
			down = -down;
		}
		if (up < 0) {
			int multiples = (-up + down - 1) / down;
			num -= multiples;
			up += multiples * down;
		}
		if (up >= down) {
			int multiples = up / down;
			num += multiples;
			up -= multiples * down;
		}
		int gcd = gcd(up, down);
		up /= gcd;
		down /= gcd;
		if (up == 0) down = 1;
		return new int[]{ num, up, down };
	}
	private static int gcd(int a, int b) {
		if (b == 0) return a;
		return gcd(b, a % b);
	}
}
