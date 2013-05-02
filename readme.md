# Acolyte

Acolyte is a JDBC driver designed for cases like mockup, testing, or any case you would like to be able to handle JDBC query by hand (or maybe that's only Chmeee's son on the Ringworld).

## Requirements

* Java 1.6+

## Usage

Acolyte can be used in SBT projects adding dependency `"cchantep" %% "acolyte" % "VERSION"` (coming on a repository).

### Code

Acolyte driver behaves as any other JDBC driver, that's to say you can get a connection from, by using the well-known `java.sql.DriverManager.getConnection(jdbcUrl)` (and its variants).

JDBC URL should match `"jdbc:acolyte:anything-you-want"`.

## Build

Alohura can be built from these sources using SBT (0.12.2+): `sbt publish`
