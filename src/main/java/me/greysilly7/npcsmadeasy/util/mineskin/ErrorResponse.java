package me.greysilly7.npcsmadeasy.util.mineskin;

import java.util.List;

public record ErrorResponse(
    boolean success,
    List<ErrorDetail> errors,
    List<ErrorDetail> warnings,
    List<ErrorDetail> messages,
    Links links) {
  public record ErrorDetail(String code, String message) {
  }

  public record Links(String self) {
  }
}
