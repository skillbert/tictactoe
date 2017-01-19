package common;

public enum Mark {
	EMPTY, RED, YELLOW;

	// TODO maybe switch to ints altogether
	public static Mark fromInt(int i) {
		switch (i) {
		case 0:
			return Mark.RED;
		case 1:
			return Mark.YELLOW;
		}
		throw new RuntimeException("Invalid mark number " + i);
	}

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
