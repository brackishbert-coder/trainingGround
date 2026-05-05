package TheGame;

public enum Status {

	GO(0), T0I0(1), CC0(2), T0I1(3), INCOMETAX(4), RR0(5), T1I0(6), C0(7), T1I1(8), T1I2(9), JAIL(10),

	T2I0(11), U0(12), T2I1(13), T2I2(14), RR1(15), T3I0(16), CC1(17), T3I1(18), T3I2(19), FREEPARKING(20),

	T4I0(21), C1(22), T4I1(23), T4I2(24), RR2(25), T5I0(26), T5I1(27), U1(28), T5I2(29), GOTOJAIL(30),

	T6I0(31), T6I1(32), CC2(33), T6I2(34), RR3(35), C2(36), FIN(37), ST(38), FINLEGAL(39),

	C2UB(40),

	T0I2(41), T3I0L(42), T5I0L(43), U2(44), U3(45);

	private int numVal;

	Status(int i) {
		this.numVal = i;
	}

	public int getNumVal() {
		return numVal;
	}

	public static Status getStatus(int value) {
		Status toreturn = null;

		switch (value) {
		case 0:

			toreturn = GO;
			break;

		case 1:

			toreturn = T0I0;
			break;

		case 2:

			toreturn = CC0;
			break;
		case 3:

			toreturn = T0I1;
			break;
		case 4:

			toreturn = INCOMETAX;
			break;
		case 5:

			toreturn = RR0;
			break;
		case 6:

			toreturn = T1I0;
			break;
		case 7:

			toreturn = C0;
			break;
		case 8:

			toreturn = T1I1;
			break;
		case 9:

			toreturn = T1I2;
			break;
		case 10:

			toreturn = JAIL;
			break;
		case 11:

			toreturn = T2I0;
			break;
		case 12:

			toreturn = U0;
			break;
		case 13:

			toreturn = T2I1;
			break;
		case 14:

			toreturn = T2I2;
			break;
		case 15:

			toreturn = RR1;
			break;
		case 16:

			toreturn = T3I0;
			break;
		case 17:

			toreturn = CC1;
			break;
		case 18:

			toreturn = T3I1;
			break;
		case 19:

			toreturn = T3I2;
			break;
		case 20:

			toreturn = FREEPARKING;
			break;
		case 21:

			toreturn = T4I0;
			break;
		case 22:

			toreturn = C1;
			break;
		case 23:

			toreturn = T4I1;
			break;
		case 24:

			toreturn = T4I2;
			break;
		case 25:

			toreturn = RR2;
			break;
		case 26:

			toreturn = T5I0;
			break;
		case 27:

			toreturn = T5I1;
			break;
		case 28:

			toreturn = U1;
			break;
		case 29:

			toreturn = T5I2;
			break;
		case 30:

			toreturn = GOTOJAIL;
			break;
		case 31:

			toreturn = T6I0;
			break;
		case 32:

			toreturn = T6I1;
			break;
		case 33:

			toreturn = CC2;
			break;
		case 34:

			toreturn = T6I2;
			break;
		case 35:

			toreturn = RR3;
			break;
		case 36:

			toreturn = C2;
			break;
		case 37:

			toreturn = FIN;
			break;
		case 38:

			toreturn = ST;
			break;
		case 39:

			toreturn = FINLEGAL;
			break;
		case 40:

			toreturn = C2UB;
			break;
		case 41:

			toreturn = T0I2;
			break;
		case 42:

			toreturn = T3I0L;
		case 43:

			toreturn = T5I0L;
			break;
		case 44:

			toreturn = U2;
			break;
		case 45:

			toreturn = U3;

			break;

		default:
			break;
		}

		return toreturn;
	}

	public static Status getStatus(String name) {
		Status toreturn = null;
		switch (name) {
		case "PASSGO":
			toreturn = GO;
			break;
		case "Triplet0I0":
			toreturn = T0I0;
			break;
		case "CommieChest0":
			toreturn = CC0;
			break;
		case "Triplet0I1":
			toreturn = T0I1;
			break;
		case "IncomeTax":
			toreturn = INCOMETAX;
			break;
		case "RailRoad0":
			toreturn = RR0;
			break;
		case "Triplet1I0":
			toreturn = T1I0;
			break;
		case "Chance0":
			toreturn = C0;
			break;
		case "Triplet1I1":
			toreturn = T1I1;
			break;
		case "Triplet1I2":
			toreturn = T1I2;
			break;
		case "JAIL":
			toreturn = JAIL;
			break;
		case "Triplet2I0":
			toreturn = T2I0;
			break;
		case "Utilities0":
			toreturn = U0;
			break;
		case "Triplet2I1":
			toreturn = T2I1;
			break;
		case "Triplet2I2":
			toreturn = T2I2;
			break;
		case "RailRoad1":
			toreturn = RR1;
			break;
		case "Triplet3I0":
			toreturn = T3I0;
			break;
		case "CommieChest1":
			toreturn = CC1;
			break;
		case "Triplet3I1":
			toreturn = T3I1;
			break;
		case "Triplet3I2":
			toreturn = T3I2;
			break;
		case "FreeParking":
			toreturn = FREEPARKING;
			break;
		case "Triplet4I0":
			toreturn = T4I0;
			break;
		case "Chance1":
			toreturn = C1;
			break;
		case "Triplet4I1":
			toreturn = T4I1;
			break;
		case "Triplet4I2":
			toreturn = T4I2;
			break;
		case "RailRoad2":
			toreturn = RR2;
			break;
		case "Triplet5I0":
			toreturn = T5I0;
			break;
		case "Triplet5I1":
			toreturn = T5I1;
			break;
		case "Utilities1":
			toreturn = U1;
			break;
		case "Triplet5I2":
			toreturn = T5I2;
			break;
		case "GOTOJAIL":
			toreturn = GOTOJAIL;
			break;
		case "Triplet6I0":
			toreturn = T6I0;
			break;
		case "Triplet6I1":
			toreturn = T6I1;
			break;
		case "CommieChest2":
			toreturn = CC2;
			break;
		case "Triplet6I2":
			toreturn = T6I2;
			break;
		case "RailRoad3":
			toreturn = RR3;
			break;
		case "Chance2":
			toreturn = C2;
			break;
		case "FIN":
			toreturn = FIN;
			break;

		case "SuperTax":
			toreturn = ST;
			break;

		case "FinLegal":
			toreturn = FINLEGAL;
			break;

		default:
			toreturn = GO;
			break;
		}

		return toreturn;
	}

	public static Status getNextStatus(Status playerStatus, int lastRoll) {
		int pos = playerStatus.getNumVal() + lastRoll;
		if (pos <= 39) {
			return Status.getStatus(pos);

		} else {
			return Status.getStatus(pos - 40);

		}

	}

}
