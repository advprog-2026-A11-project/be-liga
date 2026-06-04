package id.ac.ui.cs.advprog.liga.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

@SuppressWarnings("checkstyle:MethodName")
class SeasonTest {

  @Test
  void constructor_generatesNonNullId() {
    Season season = new Season();
    assertNotNull(season.getId());
  }

  @Test
  void constructor_setsActiveTrueByDefault() {
    Season season = new Season();
    assertTrue(season.isActive());
  }

  @Test
  void constructor_setsStartDateToNow() {
    Season season = new Season();
    assertNotNull(season.getStartDate());
  }

  @Test
  void constructor_setsEndDateToNull() {
    Season season = new Season();
    assertNull(season.getEndDate());
  }

  @Test
  void setters_updateFields() {
    Season season = new Season();
    season.setSeasonNumber(3);
    season.setActive(false);

    assertEquals(3, season.getSeasonNumber());
    assertEquals(false, season.isActive());
  }

  @Test
  void twoSeasons_haveDistinctIds() {
    Season first = new Season();
    Season second = new Season();
    assertTrue(!first.getId().equals(second.getId()));
  }
}