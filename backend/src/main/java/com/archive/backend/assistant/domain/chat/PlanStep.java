package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** How a change of a plan being made went, by its position counted from 1; or that the editor stopped the plan. */
public sealed interface PlanStep {

    @ValueObject
    record Started(int position) implements PlanStep {}

    @ValueObject
    record Done(int position, String result) implements PlanStep {}

    @ValueObject
    record Failed(int position, String error) implements PlanStep {}

    @ValueObject
    record Stopped() implements PlanStep {}
}
