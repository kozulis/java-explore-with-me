package ru.practicum.main.event.model;

import lombok.*;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.event.utils.EventState;
import ru.practicum.main.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    private Category category;

    @Column(name = "confirmed_request")
    private Long confirmedRequests;

    @Column(name = "created_on")
    private LocalDateTime createdOn; //default = NOW ?

    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator")
    private User initiator;

    private Float lat;

    private Float lon;

    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventState state;

    private String title;

    //TODO ватафак?
//    @ManyToMany(mappedBy = "events")
//    private List<Compilation> compilations;

    @Transient
    private Long views;

}
