--
-- jnobfit_userQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: custom_parameters; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.custom_parameters (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(500),
    is_active boolean NOT NULL,
    is_favorite boolean NOT NULL,
    is_global boolean NOT NULL,
    is_trackable boolean NOT NULL,
    metric_aggregation character varying(20),
    name character varying(100) NOT NULL,
    parameter_type character varying(50) NOT NULL,
    unit character varying(20),
    updated_at timestamp(6) without time zone NOT NULL,
    usage_count integer,
    owner_id bigint,
    CONSTRAINT custom_parameters_metric_aggregation_check CHECK (((metric_aggregation)::text = ANY ((ARRAY['SUM'::character varying, 'MAX'::character varying, 'MIN'::character varying, 'AVG'::character varying, 'LAST'::character varying])::text[]))),
    CONSTRAINT custom_parameters_parameter_type_check CHECK (((parameter_type)::text = ANY ((ARRAY['NUMBER'::character varying, 'INTEGER'::character varying, 'TEXT'::character varying, 'BOOLEAN'::character varying, 'DURATION'::character varying, 'DISTANCE'::character varying, 'PERCENTAGE'::character varying])::text[])))
);


ALTER TABLE public.custom_parameters OWNER TO jnobfit_user;

--
-- Name: custom_parameters_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.custom_parameters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.custom_parameters_id_seq OWNER TO jnobfit_user;

--
-- Name: custom_parameters_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.custom_parameters_id_seq OWNED BY public.custom_parameters.id;


--
-- Name: exercise_categories; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.exercise_categories (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(500),
    display_order integer,
    is_active boolean NOT NULL,
    is_predefined boolean NOT NULL,
    is_public boolean NOT NULL,
    name character varying(100) NOT NULL,
    parent_category_id bigint,
    updated_at timestamp(6) without time zone NOT NULL,
    usage_count integer,
    owner_id bigint,
    sport_id bigint
);


ALTER TABLE public.exercise_categories OWNER TO jnobfit_user;

--
-- Name: exercise_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.exercise_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exercise_categories_id_seq OWNER TO jnobfit_user;

--
-- Name: exercise_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.exercise_categories_id_seq OWNED BY public.exercise_categories.id;


--
-- Name: exercise_category_mapping; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.exercise_category_mapping (
    exercise_id bigint NOT NULL,
    category_id bigint NOT NULL
);


ALTER TABLE public.exercise_category_mapping OWNER TO jnobfit_user;

--
-- Name: exercise_metrics; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.exercise_metrics (
    id bigint NOT NULL,
    aggregation character varying(20) NOT NULL,
    granularity character varying(20) NOT NULL,
    period_end date NOT NULL,
    period_key character varying(20) NOT NULL,
    period_start date NOT NULL,
    recalculated_at timestamp(6) without time zone NOT NULL,
    sample_count integer NOT NULL,
    value double precision NOT NULL,
    exercise_id bigint,
    parameter_id bigint NOT NULL,
    user_id bigint NOT NULL,
    CONSTRAINT exercise_metrics_aggregation_check CHECK (((aggregation)::text = ANY ((ARRAY['SUM'::character varying, 'MAX'::character varying, 'MIN'::character varying, 'AVG'::character varying, 'LAST'::character varying])::text[]))),
    CONSTRAINT exercise_metrics_granularity_check CHECK (((granularity)::text = ANY ((ARRAY['SESSION'::character varying, 'DAY'::character varying, 'WEEK'::character varying, 'MONTH'::character varying, 'ALL_TIME'::character varying])::text[])))
);


ALTER TABLE public.exercise_metrics OWNER TO jnobfit_user;

--
-- Name: exercise_metrics_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.exercise_metrics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exercise_metrics_id_seq OWNER TO jnobfit_user;

--
-- Name: exercise_metrics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.exercise_metrics_id_seq OWNED BY public.exercise_metrics.id;


--
-- Name: exercise_ratings; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.exercise_ratings (
    id bigint NOT NULL,
    rated_at timestamp(6) without time zone NOT NULL,
    rating double precision NOT NULL,
    exercise_id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.exercise_ratings OWNER TO jnobfit_user;

--
-- Name: exercise_ratings_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.exercise_ratings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exercise_ratings_id_seq OWNER TO jnobfit_user;

--
-- Name: exercise_ratings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.exercise_ratings_id_seq OWNED BY public.exercise_ratings.id;


--
-- Name: exercise_sports; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.exercise_sports (
    exercise_id bigint NOT NULL,
    sport_id bigint NOT NULL
);


ALTER TABLE public.exercise_sports OWNER TO jnobfit_user;

--
-- Name: exercise_supported_parameters; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.exercise_supported_parameters (
    exercise_id bigint NOT NULL,
    parameter_id bigint NOT NULL
);


ALTER TABLE public.exercise_supported_parameters OWNER TO jnobfit_user;

--
-- Name: exercises; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.exercises (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description text,
    exercise_type character varying(50) NOT NULL,
    is_active boolean NOT NULL,
    is_public boolean NOT NULL,
    last_used_at timestamp(6) without time zone,
    name character varying(200) NOT NULL,
    rating double precision NOT NULL,
    rating_count integer NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    usage_count integer NOT NULL,
    created_by bigint,
    CONSTRAINT exercises_exercise_type_check CHECK (((exercise_type)::text = ANY ((ARRAY['SIMPLE'::character varying, 'WEIGHTED'::character varying, 'TIMED'::character varying, 'MIXED'::character varying, 'BODYWEIGHT'::character varying, 'DISTANCE'::character varying, 'REPETITION'::character varying, 'DURATION'::character varying, 'CIRCUIT'::character varying, 'AMRAP'::character varying, 'EMOM'::character varying, 'TABATA'::character varying])::text[])))
);


ALTER TABLE public.exercises OWNER TO jnobfit_user;

--
-- Name: exercises_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.exercises_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.exercises_id_seq OWNER TO jnobfit_user;

--
-- Name: exercises_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.exercises_id_seq OWNED BY public.exercises.id;


--
-- Name: metric_calculation_jobs; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.metric_calculation_jobs (
    id bigint NOT NULL,
    attempts integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    error_message text,
    processing_finished_at timestamp(6) without time zone,
    processing_started_at timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    session_id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.metric_calculation_jobs OWNER TO jnobfit_user;

--
-- Name: metric_calculation_jobs_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.metric_calculation_jobs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.metric_calculation_jobs_id_seq OWNER TO jnobfit_user;

--
-- Name: metric_calculation_jobs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.metric_calculation_jobs_id_seq OWNED BY public.metric_calculation_jobs.id;


--
-- Name: package_items; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.package_items (
    id bigint NOT NULL,
    display_order integer,
    item_type character varying(30) NOT NULL,
    notes text,
    category_id bigint,
    exercise_id bigint,
    package_id bigint NOT NULL,
    parameter_id bigint,
    routine_id bigint,
    sport_id bigint,
    CONSTRAINT package_items_item_type_check CHECK (((item_type)::text = ANY ((ARRAY['SPORT'::character varying, 'PARAMETER'::character varying, 'ROUTINE'::character varying, 'EXERCISE'::character varying, 'CATEGORY'::character varying])::text[])))
);


ALTER TABLE public.package_items OWNER TO jnobfit_user;

--
-- Name: package_items_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.package_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.package_items_id_seq OWNER TO jnobfit_user;

--
-- Name: package_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.package_items_id_seq OWNED BY public.package_items.id;


--
-- Name: packages; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.packages (
    id bigint NOT NULL,
    changelog text,
    created_at timestamp(6) without time zone NOT NULL,
    currency character varying(3),
    description text,
    download_count integer NOT NULL,
    is_free boolean NOT NULL,
    name character varying(200) NOT NULL,
    package_type character varying(30) NOT NULL,
    price numeric(10,2),
    rating double precision,
    rating_count integer NOT NULL,
    requires_subscription character varying(20) NOT NULL,
    slug character varying(200) NOT NULL,
    status character varying(20) NOT NULL,
    tags text,
    thumbnail_url character varying(512),
    updated_at timestamp(6) without time zone NOT NULL,
    version character varying(20) NOT NULL,
    created_by bigint,
    CONSTRAINT packages_package_type_check CHECK (((package_type)::text = ANY ((ARRAY['SPORT_PACK'::character varying, 'PARAMETER_PACK'::character varying, 'ROUTINE_PACK'::character varying, 'EXERCISE_PACK'::character varying, 'MIXED'::character varying])::text[]))),
    CONSTRAINT packages_requires_subscription_check CHECK (((requires_subscription)::text = ANY ((ARRAY['FREE'::character varying, 'STANDARD'::character varying, 'PREMIUM'::character varying])::text[]))),
    CONSTRAINT packages_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying, 'DEPRECATED'::character varying, 'SUSPENDED'::character varying])::text[])))
);


ALTER TABLE public.packages OWNER TO jnobfit_user;

--
-- Name: packages_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.packages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.packages_id_seq OWNER TO jnobfit_user;

--
-- Name: packages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.packages_id_seq OWNED BY public.packages.id;


--
-- Name: personal_records; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.personal_records (
    id bigint NOT NULL,
    achieved_date date NOT NULL,
    aggregation character varying(20) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    previous_record double precision,
    progress_percentage double precision,
    value double precision NOT NULL,
    exercise_id bigint,
    parameter_id bigint NOT NULL,
    set_execution_id bigint,
    user_id bigint NOT NULL,
    CONSTRAINT personal_records_aggregation_check CHECK (((aggregation)::text = ANY ((ARRAY['SUM'::character varying, 'MAX'::character varying, 'MIN'::character varying, 'AVG'::character varying, 'LAST'::character varying])::text[])))
);


ALTER TABLE public.personal_records OWNER TO jnobfit_user;

--
-- Name: personal_records_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.personal_records_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.personal_records_id_seq OWNER TO jnobfit_user;

--
-- Name: personal_records_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.personal_records_id_seq OWNED BY public.personal_records.id;


--
-- Name: progression_snapshots; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.progression_snapshots (
    id bigint NOT NULL,
    avg_value double precision,
    change_pct double precision,
    estimated_1rm double precision,
    granularity character varying(20) NOT NULL,
    max_value double precision,
    min_value double precision,
    period_end date NOT NULL,
    period_start date NOT NULL,
    recalculated_at timestamp(6) without time zone NOT NULL,
    sample_count integer NOT NULL,
    sum_value double precision,
    exercise_id bigint,
    parameter_id bigint NOT NULL,
    user_id bigint NOT NULL,
    CONSTRAINT progression_snapshots_granularity_check CHECK (((granularity)::text = ANY ((ARRAY['SESSION'::character varying, 'DAY'::character varying, 'WEEK'::character varying, 'MONTH'::character varying, 'ALL_TIME'::character varying])::text[])))
);


ALTER TABLE public.progression_snapshots OWNER TO jnobfit_user;

--
-- Name: progression_snapshots_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.progression_snapshots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.progression_snapshots_id_seq OWNER TO jnobfit_user;

--
-- Name: progression_snapshots_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.progression_snapshots_id_seq OWNED BY public.progression_snapshots.id;


--
-- Name: routine_exercise_parameters; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.routine_exercise_parameters (
    id bigint NOT NULL,
    default_value double precision,
    duration_value bigint,
    integer_value integer,
    max_value double precision,
    min_value double precision,
    numeric_value double precision,
    string_value character varying(255),
    parameter_id bigint NOT NULL,
    routine_exercise_id bigint NOT NULL
);


ALTER TABLE public.routine_exercise_parameters OWNER TO jnobfit_user;

--
-- Name: routine_exercise_parameters_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.routine_exercise_parameters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.routine_exercise_parameters_id_seq OWNER TO jnobfit_user;

--
-- Name: routine_exercise_parameters_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.routine_exercise_parameters_id_seq OWNED BY public.routine_exercise_parameters.id;


--
-- Name: routine_exercises; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.routine_exercises (
    id bigint NOT NULL,
    amrap_duration_seconds integer,
    circuit_group_id character varying(50),
    circuit_round_count integer,
    day_of_week character varying(10),
    emom_interval_seconds integer,
    emom_total_rounds integer,
    notes text,
    "position" integer NOT NULL,
    rest_after_exercise integer,
    session_number integer NOT NULL,
    session_order integer,
    super_set_group_id character varying(50),
    tabata_rest_seconds integer,
    tabata_rounds integer,
    tabata_work_seconds integer,
    exercise_id bigint NOT NULL,
    routine_id bigint NOT NULL,
    CONSTRAINT routine_exercises_day_of_week_check CHECK (((day_of_week)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[])))
);


ALTER TABLE public.routine_exercises OWNER TO jnobfit_user;

--
-- Name: routine_exercises_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.routine_exercises_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.routine_exercises_id_seq OWNER TO jnobfit_user;

--
-- Name: routine_exercises_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.routine_exercises_id_seq OWNED BY public.routine_exercises.id;


--
-- Name: routine_import_log; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.routine_import_log (
    id bigint NOT NULL,
    import_warnings text,
    imported_at timestamp(6) without time zone NOT NULL,
    source_reference character varying(500),
    source_type character varying(30) NOT NULL,
    imported_by bigint NOT NULL,
    imported_routine_id bigint NOT NULL,
    CONSTRAINT routine_import_log_source_type_check CHECK (((source_type)::text = ANY ((ARRAY['FILE'::character varying, 'SHARE_LINK'::character varying, 'PACK'::character varying, 'MARKETPLACE'::character varying])::text[])))
);


ALTER TABLE public.routine_import_log OWNER TO jnobfit_user;

--
-- Name: routine_import_log_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.routine_import_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.routine_import_log_id_seq OWNER TO jnobfit_user;

--
-- Name: routine_import_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.routine_import_log_id_seq OWNED BY public.routine_import_log.id;


--
-- Name: routine_set_parameters; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.routine_set_parameters (
    id bigint NOT NULL,
    duration_value bigint,
    integer_value integer,
    numeric_value double precision,
    repetitions integer,
    parameter_id bigint NOT NULL,
    set_template_id bigint NOT NULL
);


ALTER TABLE public.routine_set_parameters OWNER TO jnobfit_user;

--
-- Name: routine_set_parameters_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.routine_set_parameters_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.routine_set_parameters_seq OWNER TO jnobfit_user;

--
-- Name: routine_set_templates; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.routine_set_templates (
    id bigint NOT NULL,
    group_id character varying(255),
    "position" integer,
    rest_after_set integer,
    set_type character varying(255),
    sub_set_number integer,
    routine_exercise_id bigint NOT NULL,
    CONSTRAINT routine_set_templates_set_type_check CHECK (((set_type)::text = ANY ((ARRAY['NORMAL'::character varying, 'WARM_UP'::character varying, 'DROP_SET'::character varying, 'SUPER_SET'::character varying, 'GIANT_SET'::character varying, 'PYRAMID'::character varying, 'REVERSE_PYRAMID'::character varying, 'CLUSTER'::character varying, 'REST_PAUSE'::character varying, 'ECCENTRIC'::character varying, 'ISOMETRIC'::character varying])::text[])))
);


ALTER TABLE public.routine_set_templates OWNER TO jnobfit_user;

--
-- Name: routine_set_templates_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.routine_set_templates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.routine_set_templates_id_seq OWNER TO jnobfit_user;

--
-- Name: routine_set_templates_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.routine_set_templates_id_seq OWNED BY public.routine_set_templates.id;


--
-- Name: routines; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.routines (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    currency character varying(3),
    description text,
    export_key uuid,
    goal character varying(500),
    is_active boolean NOT NULL,
    is_public boolean NOT NULL,
    is_template boolean NOT NULL,
    last_used_at timestamp(6) without time zone,
    name character varying(200) NOT NULL,
    price numeric(10,2),
    sessions_per_week integer,
    times_purchased integer NOT NULL,
    training_days text,
    updated_at timestamp(6) without time zone NOT NULL,
    version character varying(20),
    original_routine_id bigint,
    package_id bigint,
    sport_id bigint,
    user_id bigint NOT NULL,
    CONSTRAINT routines_sessions_per_week_check CHECK (((sessions_per_week >= 1) AND (sessions_per_week <= 7)))
);


ALTER TABLE public.routines OWNER TO jnobfit_user;

--
-- Name: routines_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.routines_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.routines_id_seq OWNER TO jnobfit_user;

--
-- Name: routines_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.routines_id_seq OWNED BY public.routines.id;


--
-- Name: session_exercises; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.session_exercises (
    id bigint NOT NULL,
    completed_at timestamp(6) without time zone,
    personal_notes character varying(2000),
    started_at timestamp(6) without time zone,
    status character varying(255),
    exercise_id bigint,
    session_id bigint,
    exercise_order integer,
    CONSTRAINT session_exercises_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'SKIPPED'::character varying, 'INCOMPLETE'::character varying])::text[])))
);


ALTER TABLE public.session_exercises OWNER TO jnobfit_user;

--
-- Name: session_exercises_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.session_exercises_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.session_exercises_id_seq OWNER TO jnobfit_user;

--
-- Name: session_exercises_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.session_exercises_id_seq OWNED BY public.session_exercises.id;


--
-- Name: session_metrics; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.session_metrics (
    id bigint NOT NULL,
    avg_rpe double precision,
    completed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    duration_seconds bigint,
    exercises_completed integer NOT NULL,
    performance_score integer,
    personal_records_count integer NOT NULL,
    plan_adherence_pct double precision,
    session_date date NOT NULL,
    sets_completed integer NOT NULL,
    sets_skipped integer NOT NULL,
    started_at timestamp(6) without time zone NOT NULL,
    total_volume_kg double precision,
    routine_id bigint,
    session_id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.session_metrics OWNER TO jnobfit_user;

--
-- Name: session_metrics_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.session_metrics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.session_metrics_id_seq OWNER TO jnobfit_user;

--
-- Name: session_metrics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.session_metrics_id_seq OWNED BY public.session_metrics.id;


--
-- Name: set_execution_parameters; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.set_execution_parameters (
    id bigint NOT NULL,
    duration_value bigint,
    integer_value integer,
    is_personal_record boolean NOT NULL,
    numeric_value double precision,
    string_value text,
    parameter_id bigint NOT NULL,
    set_execution_id bigint NOT NULL
);


ALTER TABLE public.set_execution_parameters OWNER TO jnobfit_user;

--
-- Name: set_execution_parameters_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.set_execution_parameters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.set_execution_parameters_id_seq OWNER TO jnobfit_user;

--
-- Name: set_execution_parameters_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.set_execution_parameters_id_seq OWNED BY public.set_execution_parameters.id;


--
-- Name: set_executions; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.set_executions (
    id bigint NOT NULL,
    actual_rest_seconds integer,
    completed_at timestamp(6) without time zone,
    notes text,
    "position" integer NOT NULL,
    set_type character varying(30),
    started_at timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    session_exercise_id bigint NOT NULL,
    set_template_id bigint,
    CONSTRAINT set_executions_set_type_check CHECK (((set_type)::text = ANY ((ARRAY['NORMAL'::character varying, 'WARM_UP'::character varying, 'DROP_SET'::character varying, 'SUPER_SET'::character varying, 'GIANT_SET'::character varying, 'PYRAMID'::character varying, 'REVERSE_PYRAMID'::character varying, 'CLUSTER'::character varying, 'REST_PAUSE'::character varying, 'ECCENTRIC'::character varying, 'ISOMETRIC'::character varying])::text[]))),
    CONSTRAINT set_executions_status_check CHECK (((status)::text = ANY ((ARRAY['COMPLETED'::character varying, 'SKIPPED'::character varying, 'FAILED'::character varying, 'PARTIAL'::character varying])::text[])))
);


ALTER TABLE public.set_executions OWNER TO jnobfit_user;

--
-- Name: set_executions_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.set_executions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.set_executions_id_seq OWNER TO jnobfit_user;

--
-- Name: set_executions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.set_executions_id_seq OWNED BY public.set_executions.id;


--
-- Name: sports; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.sports (
    id bigint NOT NULL,
    is_predefined boolean NOT NULL,
    name character varying(255) NOT NULL,
    parameter_template text,
    source_type character varying(255) NOT NULL,
    created_by bigint,
    CONSTRAINT sports_source_type_check CHECK (((source_type)::text = ANY ((ARRAY['OFFICIAL'::character varying, 'USER_CREATED'::character varying, 'THIRD_PARTY'::character varying, 'IMPORTED'::character varying])::text[])))
);


ALTER TABLE public.sports OWNER TO jnobfit_user;

--
-- Name: sports_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.sports_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sports_id_seq OWNER TO jnobfit_user;

--
-- Name: sports_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.sports_id_seq OWNED BY public.sports.id;


--
-- Name: spring_session; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.spring_session (
    primary_id character(36) NOT NULL,
    session_id character(36) NOT NULL,
    creation_time bigint NOT NULL,
    last_access_time bigint NOT NULL,
    max_inactive_interval integer NOT NULL,
    expiry_time bigint NOT NULL,
    principal_name character varying(100)
);


ALTER TABLE public.spring_session OWNER TO jnobfit_user;

--
-- Name: spring_session_attributes; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.spring_session_attributes (
    session_primary_id character(36) NOT NULL,
    attribute_name character varying(200) NOT NULL,
    attribute_bytes bytea NOT NULL
);


ALTER TABLE public.spring_session_attributes OWNER TO jnobfit_user;

--
-- Name: subscription_history; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.subscription_history (
    id bigint NOT NULL,
    changed_at timestamp(6) without time zone NOT NULL,
    from_type character varying(20),
    notes character varying(500),
    performed_by character varying(50) NOT NULL,
    reason character varying(30),
    to_type character varying(20) NOT NULL,
    user_id bigint NOT NULL,
    CONSTRAINT subscription_history_from_type_check CHECK (((from_type)::text = ANY ((ARRAY['FREE'::character varying, 'STANDARD'::character varying, 'PREMIUM'::character varying])::text[]))),
    CONSTRAINT subscription_history_reason_check CHECK (((reason)::text = ANY ((ARRAY['UPGRADE'::character varying, 'DOWNGRADE'::character varying, 'TRIAL_START'::character varying, 'TRIAL_END'::character varying, 'CANCELLATION'::character varying, 'REACTIVATION'::character varying, 'PAYMENT_FAILED'::character varying, 'ADMIN_OVERRIDE'::character varying, 'PROMO_APPLIED'::character varying])::text[]))),
    CONSTRAINT subscription_history_to_type_check CHECK (((to_type)::text = ANY ((ARRAY['FREE'::character varying, 'STANDARD'::character varying, 'PREMIUM'::character varying])::text[])))
);


ALTER TABLE public.subscription_history OWNER TO jnobfit_user;

--
-- Name: subscription_history_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.subscription_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subscription_history_id_seq OWNER TO jnobfit_user;

--
-- Name: subscription_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.subscription_history_id_seq OWNED BY public.subscription_history.id;


--
-- Name: subscription_limits; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.subscription_limits (
    id bigint NOT NULL,
    advanced_analytics boolean NOT NULL,
    basic_analytics boolean NOT NULL,
    can_export_routines boolean NOT NULL,
    can_import_routines boolean NOT NULL,
    free_packs_only boolean NOT NULL,
    history_days integer,
    marketplace_read boolean NOT NULL,
    marketplace_sell boolean NOT NULL,
    max_custom_categories integer,
    max_custom_exercises integer,
    max_custom_parameters integer,
    max_custom_sports integer,
    max_exercises_per_routine integer,
    max_routines integer,
    max_sets_per_exercise integer,
    tier character varying(20) NOT NULL
);


ALTER TABLE public.subscription_limits OWNER TO jnobfit_user;

--
-- Name: subscription_limits_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.subscription_limits_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subscription_limits_id_seq OWNER TO jnobfit_user;

--
-- Name: subscription_limits_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.subscription_limits_id_seq OWNED BY public.subscription_limits.id;


--
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.subscriptions (
    id bigint NOT NULL,
    auto_renew boolean NOT NULL,
    cancel_reason character varying(500),
    cancelled_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    end_date date NOT NULL,
    external_subscription_id character varying(200),
    payment_provider character varying(50),
    start_date date NOT NULL,
    status character varying(20) NOT NULL,
    subscription_type character varying(20) NOT NULL,
    trial_ends_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    limits_id bigint NOT NULL,
    user_id bigint NOT NULL,
    CONSTRAINT subscriptions_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'TRIAL'::character varying, 'EXPIRED'::character varying, 'CANCELLED'::character varying, 'PAST_DUE'::character varying])::text[]))),
    CONSTRAINT subscriptions_subscription_type_check CHECK (((subscription_type)::text = ANY ((ARRAY['FREE'::character varying, 'STANDARD'::character varying, 'PREMIUM'::character varying])::text[])))
);


ALTER TABLE public.subscriptions OWNER TO jnobfit_user;

--
-- Name: subscriptions_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.subscriptions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subscriptions_id_seq OWNER TO jnobfit_user;

--
-- Name: subscriptions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.subscriptions_id_seq OWNED BY public.subscriptions.id;


--
-- Name: user_installed_packages; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.user_installed_packages (
    id bigint NOT NULL,
    installed_at timestamp(6) without time zone NOT NULL,
    installed_version character varying(20),
    is_active boolean NOT NULL,
    purchase_price numeric(10,2),
    transaction_id character varying(200),
    package_id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.user_installed_packages OWNER TO jnobfit_user;

--
-- Name: user_installed_packages_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.user_installed_packages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_installed_packages_id_seq OWNER TO jnobfit_user;

--
-- Name: user_installed_packages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.user_installed_packages_id_seq OWNED BY public.user_installed_packages.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    email_verification_token character varying(128),
    email_verification_token_expires_at timestamp(6) without time zone,
    email_verified boolean NOT NULL,
    full_name character varying(255),
    google_id character varying(128),
    is_active boolean NOT NULL,
    last_login timestamp(6) without time zone,
    password character varying(255),
    profile_image character varying(512),
    provider character varying(32),
    role character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    version bigint,
    subscription_id bigint,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'USER'::character varying, 'GUEST'::character varying, 'MODERATOR'::character varying, 'SUPER_ADMIN'::character varying, 'BANNED'::character varying, 'DELETED'::character varying, 'COACH'::character varying, 'CONTRIBUTOR'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO jnobfit_user;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO jnobfit_user;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: workout_sessions; Type: TABLE; Schema: public; Owner: jnobfit_user
--

CREATE TABLE public.workout_sessions (
    id bigint NOT NULL,
    end_time timestamp(6) without time zone,
    performance_score integer,
    start_time timestamp(6) without time zone NOT NULL,
    total_volume double precision,
    routine_id bigint
);


ALTER TABLE public.workout_sessions OWNER TO jnobfit_user;

--
-- Name: workout_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: jnobfit_user
--

CREATE SEQUENCE public.workout_sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.workout_sessions_id_seq OWNER TO jnobfit_user;

--
-- Name: workout_sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jnobfit_user
--

ALTER SEQUENCE public.workout_sessions_id_seq OWNED BY public.workout_sessions.id;


--
-- Name: custom_parameters id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.custom_parameters ALTER COLUMN id SET DEFAULT nextval('public.custom_parameters_id_seq'::regclass);


--
-- Name: exercise_categories id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_categories ALTER COLUMN id SET DEFAULT nextval('public.exercise_categories_id_seq'::regclass);


--
-- Name: exercise_metrics id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_metrics ALTER COLUMN id SET DEFAULT nextval('public.exercise_metrics_id_seq'::regclass);


--
-- Name: exercise_ratings id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_ratings ALTER COLUMN id SET DEFAULT nextval('public.exercise_ratings_id_seq'::regclass);


--
-- Name: exercises id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercises ALTER COLUMN id SET DEFAULT nextval('public.exercises_id_seq'::regclass);


--
-- Name: metric_calculation_jobs id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.metric_calculation_jobs ALTER COLUMN id SET DEFAULT nextval('public.metric_calculation_jobs_id_seq'::regclass);


--
-- Name: package_items id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.package_items ALTER COLUMN id SET DEFAULT nextval('public.package_items_id_seq'::regclass);


--
-- Name: packages id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.packages ALTER COLUMN id SET DEFAULT nextval('public.packages_id_seq'::regclass);


--
-- Name: personal_records id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.personal_records ALTER COLUMN id SET DEFAULT nextval('public.personal_records_id_seq'::regclass);


--
-- Name: progression_snapshots id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.progression_snapshots ALTER COLUMN id SET DEFAULT nextval('public.progression_snapshots_id_seq'::regclass);


--
-- Name: routine_exercise_parameters id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_exercise_parameters ALTER COLUMN id SET DEFAULT nextval('public.routine_exercise_parameters_id_seq'::regclass);


--
-- Name: routine_exercises id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_exercises ALTER COLUMN id SET DEFAULT nextval('public.routine_exercises_id_seq'::regclass);


--
-- Name: routine_import_log id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_import_log ALTER COLUMN id SET DEFAULT nextval('public.routine_import_log_id_seq'::regclass);


--
-- Name: routine_set_templates id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_set_templates ALTER COLUMN id SET DEFAULT nextval('public.routine_set_templates_id_seq'::regclass);


--
-- Name: routines id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routines ALTER COLUMN id SET DEFAULT nextval('public.routines_id_seq'::regclass);


--
-- Name: session_exercises id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_exercises ALTER COLUMN id SET DEFAULT nextval('public.session_exercises_id_seq'::regclass);


--
-- Name: session_metrics id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_metrics ALTER COLUMN id SET DEFAULT nextval('public.session_metrics_id_seq'::regclass);


--
-- Name: set_execution_parameters id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.set_execution_parameters ALTER COLUMN id SET DEFAULT nextval('public.set_execution_parameters_id_seq'::regclass);


--
-- Name: set_executions id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.set_executions ALTER COLUMN id SET DEFAULT nextval('public.set_executions_id_seq'::regclass);


--
-- Name: sports id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.sports ALTER COLUMN id SET DEFAULT nextval('public.sports_id_seq'::regclass);


--
-- Name: subscription_history id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscription_history ALTER COLUMN id SET DEFAULT nextval('public.subscription_history_id_seq'::regclass);


--
-- Name: subscription_limits id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscription_limits ALTER COLUMN id SET DEFAULT nextval('public.subscription_limits_id_seq'::regclass);


--
-- Name: subscriptions id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscriptions ALTER COLUMN id SET DEFAULT nextval('public.subscriptions_id_seq'::regclass);


--
-- Name: user_installed_packages id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.user_installed_packages ALTER COLUMN id SET DEFAULT nextval('public.user_installed_packages_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: workout_sessions id; Type: DEFAULT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.workout_sessions ALTER COLUMN id SET DEFAULT nextval('public.workout_sessions_id_seq'::regclass);


--
-- Name: custom_parameters custom_parameters_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.custom_parameters
    ADD CONSTRAINT custom_parameters_pkey PRIMARY KEY (id);


--
-- Name: exercise_categories exercise_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_categories
    ADD CONSTRAINT exercise_categories_pkey PRIMARY KEY (id);


--
-- Name: exercise_category_mapping exercise_category_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_category_mapping
    ADD CONSTRAINT exercise_category_mapping_pkey PRIMARY KEY (exercise_id, category_id);


--
-- Name: exercise_metrics exercise_metrics_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_metrics
    ADD CONSTRAINT exercise_metrics_pkey PRIMARY KEY (id);


--
-- Name: exercise_ratings exercise_ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_ratings
    ADD CONSTRAINT exercise_ratings_pkey PRIMARY KEY (id);


--
-- Name: exercise_sports exercise_sports_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_sports
    ADD CONSTRAINT exercise_sports_pkey PRIMARY KEY (exercise_id, sport_id);


--
-- Name: exercise_supported_parameters exercise_supported_parameters_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_supported_parameters
    ADD CONSTRAINT exercise_supported_parameters_pkey PRIMARY KEY (exercise_id, parameter_id);


--
-- Name: exercises exercises_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercises
    ADD CONSTRAINT exercises_pkey PRIMARY KEY (id);


--
-- Name: metric_calculation_jobs metric_calculation_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.metric_calculation_jobs
    ADD CONSTRAINT metric_calculation_jobs_pkey PRIMARY KEY (id);


--
-- Name: package_items package_items_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.package_items
    ADD CONSTRAINT package_items_pkey PRIMARY KEY (id);


--
-- Name: packages packages_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.packages
    ADD CONSTRAINT packages_pkey PRIMARY KEY (id);


--
-- Name: personal_records personal_records_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.personal_records
    ADD CONSTRAINT personal_records_pkey PRIMARY KEY (id);


--
-- Name: progression_snapshots progression_snapshots_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.progression_snapshots
    ADD CONSTRAINT progression_snapshots_pkey PRIMARY KEY (id);


--
-- Name: routine_exercise_parameters routine_exercise_parameters_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_exercise_parameters
    ADD CONSTRAINT routine_exercise_parameters_pkey PRIMARY KEY (id);


--
-- Name: routine_exercises routine_exercises_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_exercises
    ADD CONSTRAINT routine_exercises_pkey PRIMARY KEY (id);


--
-- Name: routine_import_log routine_import_log_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_import_log
    ADD CONSTRAINT routine_import_log_pkey PRIMARY KEY (id);


--
-- Name: routine_set_parameters routine_set_parameters_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_set_parameters
    ADD CONSTRAINT routine_set_parameters_pkey PRIMARY KEY (id);


--
-- Name: routine_set_templates routine_set_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_set_templates
    ADD CONSTRAINT routine_set_templates_pkey PRIMARY KEY (id);


--
-- Name: routines routines_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routines
    ADD CONSTRAINT routines_pkey PRIMARY KEY (id);


--
-- Name: session_exercises session_exercises_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_exercises
    ADD CONSTRAINT session_exercises_pkey PRIMARY KEY (id);


--
-- Name: session_metrics session_metrics_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_metrics
    ADD CONSTRAINT session_metrics_pkey PRIMARY KEY (id);


--
-- Name: set_execution_parameters set_execution_parameters_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.set_execution_parameters
    ADD CONSTRAINT set_execution_parameters_pkey PRIMARY KEY (id);


--
-- Name: set_executions set_executions_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.set_executions
    ADD CONSTRAINT set_executions_pkey PRIMARY KEY (id);


--
-- Name: sports sports_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.sports
    ADD CONSTRAINT sports_pkey PRIMARY KEY (id);


--
-- Name: spring_session_attributes spring_session_attributes_pk; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.spring_session_attributes
    ADD CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name);


--
-- Name: spring_session spring_session_pk; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.spring_session
    ADD CONSTRAINT spring_session_pk PRIMARY KEY (primary_id);


--
-- Name: subscription_history subscription_history_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscription_history
    ADD CONSTRAINT subscription_history_pkey PRIMARY KEY (id);


--
-- Name: subscription_limits subscription_limits_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscription_limits
    ADD CONSTRAINT subscription_limits_pkey PRIMARY KEY (id);


--
-- Name: subscriptions subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: exercise_categories uk_category_name_owner; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_categories
    ADD CONSTRAINT uk_category_name_owner UNIQUE (name, owner_id, is_predefined);


--
-- Name: exercise_ratings uk_exercise_rating_user; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_ratings
    ADD CONSTRAINT uk_exercise_rating_user UNIQUE (exercise_id, user_id);


--
-- Name: session_metrics uk_ik4p6dtqmccscvlb2itdgeksm; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_metrics
    ADD CONSTRAINT uk_ik4p6dtqmccscvlb2itdgeksm UNIQUE (session_id);


--
-- Name: subscriptions uk_l3ommhd1n0tu0k2va0cbp87qe; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT uk_l3ommhd1n0tu0k2va0cbp87qe UNIQUE (user_id);


--
-- Name: subscription_limits uk_limits_tier; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscription_limits
    ADD CONSTRAINT uk_limits_tier UNIQUE (tier);


--
-- Name: exercise_metrics uk_metric_user_ex_param_agg_gran_period; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_metrics
    ADD CONSTRAINT uk_metric_user_ex_param_agg_gran_period UNIQUE (user_id, exercise_id, parameter_id, aggregation, granularity, period_key);


--
-- Name: users uk_ns8vi4ouq0uoo25pse5gos0bn; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_ns8vi4ouq0uoo25pse5gos0bn UNIQUE (subscription_id);


--
-- Name: custom_parameters uk_parameter_name_owner; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.custom_parameters
    ADD CONSTRAINT uk_parameter_name_owner UNIQUE (name, owner_id);


--
-- Name: packages uk_pkg_slug; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.packages
    ADD CONSTRAINT uk_pkg_slug UNIQUE (slug);


--
-- Name: personal_records uk_pr_user_exercise_param_agg; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.personal_records
    ADD CONSTRAINT uk_pr_user_exercise_param_agg UNIQUE (user_id, exercise_id, parameter_id, aggregation);


--
-- Name: routines uk_romwxr8jvll0yged68nb0habi; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routines
    ADD CONSTRAINT uk_romwxr8jvll0yged68nb0habi UNIQUE (export_key);


--
-- Name: progression_snapshots uk_snap_user_ex_param_gran_period; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.progression_snapshots
    ADD CONSTRAINT uk_snap_user_ex_param_gran_period UNIQUE (user_id, exercise_id, parameter_id, granularity, period_start);


--
-- Name: user_installed_packages uk_user_package; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.user_installed_packages
    ADD CONSTRAINT uk_user_package UNIQUE (user_id, package_id);


--
-- Name: user_installed_packages user_installed_packages_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.user_installed_packages
    ADD CONSTRAINT user_installed_packages_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: workout_sessions workout_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.workout_sessions
    ADD CONSTRAINT workout_sessions_pkey PRIMARY KEY (id);


--
-- Name: idx_category_active; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_category_active ON public.exercise_categories USING btree (is_active);


--
-- Name: idx_category_owner; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_category_owner ON public.exercise_categories USING btree (owner_id);


--
-- Name: idx_category_predefined; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_category_predefined ON public.exercise_categories USING btree (is_predefined);


--
-- Name: idx_category_sport; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_category_sport ON public.exercise_categories USING btree (sport_id);


--
-- Name: idx_em_exercise_param; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_em_exercise_param ON public.exercise_metrics USING btree (exercise_id, parameter_id, granularity);


--
-- Name: idx_em_user_exercise; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_em_user_exercise ON public.exercise_metrics USING btree (user_id, exercise_id);


--
-- Name: idx_em_user_gran; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_em_user_gran ON public.exercise_metrics USING btree (user_id, granularity, period_key);


--
-- Name: idx_em_user_param; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_em_user_param ON public.exercise_metrics USING btree (user_id, parameter_id);


--
-- Name: idx_exercise_active; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_exercise_active ON public.exercises USING btree (is_active);


--
-- Name: idx_exercise_created_by; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_exercise_created_by ON public.exercises USING btree (created_by);


--
-- Name: idx_exercise_public; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_exercise_public ON public.exercises USING btree (is_public);


--
-- Name: idx_exercise_rating; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_exercise_rating ON public.exercises USING btree (rating);


--
-- Name: idx_exercise_sports_exercise; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_exercise_sports_exercise ON public.exercise_sports USING btree (exercise_id);


--
-- Name: idx_exercise_sports_sport; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_exercise_sports_sport ON public.exercise_sports USING btree (sport_id);


--
-- Name: idx_exercise_type; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_exercise_type ON public.exercises USING btree (exercise_type);


--
-- Name: idx_exercise_usage; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_exercise_usage ON public.exercises USING btree (usage_count);


--
-- Name: idx_mcj_created; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_mcj_created ON public.metric_calculation_jobs USING btree (created_at);


--
-- Name: idx_mcj_session; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_mcj_session ON public.metric_calculation_jobs USING btree (session_id);


--
-- Name: idx_mcj_status; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_mcj_status ON public.metric_calculation_jobs USING btree (status);


--
-- Name: idx_mcj_user; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_mcj_user ON public.metric_calculation_jobs USING btree (user_id);


--
-- Name: idx_parameter_favorite; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_parameter_favorite ON public.custom_parameters USING btree (is_favorite);


--
-- Name: idx_parameter_global_active; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_parameter_global_active ON public.custom_parameters USING btree (is_global, is_active);


--
-- Name: idx_parameter_owner_id; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_parameter_owner_id ON public.custom_parameters USING btree (owner_id);


--
-- Name: idx_parameter_trackable; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_parameter_trackable ON public.custom_parameters USING btree (is_trackable);


--
-- Name: idx_parameter_type; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_parameter_type ON public.custom_parameters USING btree (parameter_type);


--
-- Name: idx_parameter_usage_count; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_parameter_usage_count ON public.custom_parameters USING btree (usage_count);


--
-- Name: idx_pkg_created_by; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkg_created_by ON public.packages USING btree (created_by);


--
-- Name: idx_pkg_free_status; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkg_free_status ON public.packages USING btree (is_free, status);


--
-- Name: idx_pkg_requires_sub; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkg_requires_sub ON public.packages USING btree (requires_subscription);


--
-- Name: idx_pkg_status; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkg_status ON public.packages USING btree (status);


--
-- Name: idx_pkg_type_status; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkg_type_status ON public.packages USING btree (package_type, status);


--
-- Name: idx_pkgitem_category; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkgitem_category ON public.package_items USING btree (category_id);


--
-- Name: idx_pkgitem_exercise; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkgitem_exercise ON public.package_items USING btree (exercise_id);


--
-- Name: idx_pkgitem_pack; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkgitem_pack ON public.package_items USING btree (package_id);


--
-- Name: idx_pkgitem_parameter; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkgitem_parameter ON public.package_items USING btree (parameter_id);


--
-- Name: idx_pkgitem_routine; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkgitem_routine ON public.package_items USING btree (routine_id);


--
-- Name: idx_pkgitem_sport; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkgitem_sport ON public.package_items USING btree (sport_id);


--
-- Name: idx_pkgitem_type; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pkgitem_type ON public.package_items USING btree (item_type);


--
-- Name: idx_pr_date; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pr_date ON public.personal_records USING btree (user_id, achieved_date);


--
-- Name: idx_pr_exercise_param; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pr_exercise_param ON public.personal_records USING btree (exercise_id, parameter_id);


--
-- Name: idx_pr_user_exercise; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pr_user_exercise ON public.personal_records USING btree (user_id, exercise_id);


--
-- Name: idx_pr_user_parameter; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_pr_user_parameter ON public.personal_records USING btree (user_id, parameter_id);


--
-- Name: idx_rating_exercise; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_rating_exercise ON public.exercise_ratings USING btree (exercise_id);


--
-- Name: idx_rating_user; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_rating_user ON public.exercise_ratings USING btree (user_id);


--
-- Name: idx_re_circuit_group; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_re_circuit_group ON public.routine_exercises USING btree (routine_id, circuit_group_id);


--
-- Name: idx_re_exercise; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_re_exercise ON public.routine_exercises USING btree (exercise_id);


--
-- Name: idx_re_routine; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_re_routine ON public.routine_exercises USING btree (routine_id);


--
-- Name: idx_re_session; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_re_session ON public.routine_exercises USING btree (routine_id, session_number);


--
-- Name: idx_re_superset_group; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_re_superset_group ON public.routine_exercises USING btree (routine_id, super_set_group_id);


--
-- Name: idx_ril_imported_at; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_ril_imported_at ON public.routine_import_log USING btree (imported_at);


--
-- Name: idx_ril_routine; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_ril_routine ON public.routine_import_log USING btree (imported_routine_id);


--
-- Name: idx_ril_user; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_ril_user ON public.routine_import_log USING btree (imported_by);


--
-- Name: idx_routine_active; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_routine_active ON public.routines USING btree (is_active);


--
-- Name: idx_routine_export_key; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_routine_export_key ON public.routines USING btree (export_key);


--
-- Name: idx_routine_package; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_routine_package ON public.routines USING btree (package_id);


--
-- Name: idx_routine_sport; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_routine_sport ON public.routines USING btree (sport_id);


--
-- Name: idx_routine_template_public; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_routine_template_public ON public.routines USING btree (is_template, is_public);


--
-- Name: idx_routine_user; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_routine_user ON public.routines USING btree (user_id);


--
-- Name: idx_se_position; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_se_position ON public.set_executions USING btree (session_exercise_id, "position");


--
-- Name: idx_se_session_ex; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_se_session_ex ON public.set_executions USING btree (session_exercise_id);


--
-- Name: idx_se_status; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_se_status ON public.set_executions USING btree (status);


--
-- Name: idx_sep_execution; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sep_execution ON public.set_execution_parameters USING btree (set_execution_id);


--
-- Name: idx_sep_parameter; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sep_parameter ON public.set_execution_parameters USING btree (parameter_id);


--
-- Name: idx_sep_pr; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sep_pr ON public.set_execution_parameters USING btree (set_execution_id, is_personal_record);


--
-- Name: idx_set_parameter_param; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_set_parameter_param ON public.routine_set_parameters USING btree (parameter_id);


--
-- Name: idx_set_parameter_template; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_set_parameter_template ON public.routine_set_parameters USING btree (set_template_id);


--
-- Name: idx_set_template_group; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_set_template_group ON public.routine_set_templates USING btree (routine_exercise_id, group_id);


--
-- Name: idx_set_template_position; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_set_template_position ON public.routine_set_templates USING btree (routine_exercise_id, "position");


--
-- Name: idx_set_template_routine_exercise; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_set_template_routine_exercise ON public.routine_set_templates USING btree (routine_exercise_id);


--
-- Name: idx_sm_routine; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sm_routine ON public.session_metrics USING btree (routine_id);


--
-- Name: idx_sm_session; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sm_session ON public.session_metrics USING btree (session_id);


--
-- Name: idx_sm_user_date; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sm_user_date ON public.session_metrics USING btree (user_id, session_date);


--
-- Name: idx_sm_user_routine; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sm_user_routine ON public.session_metrics USING btree (user_id, routine_id);


--
-- Name: idx_snap_param; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_snap_param ON public.progression_snapshots USING btree (parameter_id);


--
-- Name: idx_snap_period; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_snap_period ON public.progression_snapshots USING btree (user_id, granularity, period_start);


--
-- Name: idx_snap_user_ex; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_snap_user_ex ON public.progression_snapshots USING btree (user_id, exercise_id);


--
-- Name: idx_snap_user_ex_param; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_snap_user_ex_param ON public.progression_snapshots USING btree (user_id, exercise_id, parameter_id);


--
-- Name: idx_sub_end_date; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sub_end_date ON public.subscriptions USING btree (end_date, status);


--
-- Name: idx_sub_hist_changed_at; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sub_hist_changed_at ON public.subscription_history USING btree (changed_at);


--
-- Name: idx_sub_hist_user; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sub_hist_user ON public.subscription_history USING btree (user_id);


--
-- Name: idx_sub_status; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sub_status ON public.subscriptions USING btree (status);


--
-- Name: idx_sub_type_status; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sub_type_status ON public.subscriptions USING btree (subscription_type, status);


--
-- Name: idx_sub_user; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_sub_user ON public.subscriptions USING btree (user_id);


--
-- Name: idx_uip_package; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_uip_package ON public.user_installed_packages USING btree (package_id);


--
-- Name: idx_uip_user_active; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_uip_user_active ON public.user_installed_packages USING btree (user_id, is_active);


--
-- Name: idx_user_email; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_user_email ON public.users USING btree (email);


--
-- Name: idx_user_role; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX idx_user_role ON public.users USING btree (role);


--
-- Name: spring_session_ix1; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE UNIQUE INDEX spring_session_ix1 ON public.spring_session USING btree (session_id);


--
-- Name: spring_session_ix2; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX spring_session_ix2 ON public.spring_session USING btree (expiry_time);


--
-- Name: spring_session_ix3; Type: INDEX; Schema: public; Owner: jnobfit_user
--

CREATE INDEX spring_session_ix3 ON public.spring_session USING btree (principal_name);


--
-- Name: routine_set_parameters fk113rv9ykx4m7icfyoq2flhwdm; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_set_parameters
    ADD CONSTRAINT fk113rv9ykx4m7icfyoq2flhwdm FOREIGN KEY (set_template_id) REFERENCES public.routine_set_templates(id);


--
-- Name: exercise_category_mapping fk1lj8e15nlo6274976re3fc6ql; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_category_mapping
    ADD CONSTRAINT fk1lj8e15nlo6274976re3fc6ql FOREIGN KEY (category_id) REFERENCES public.exercise_categories(id);


--
-- Name: exercise_supported_parameters fk1yb9u4nb6jmqx94sx08vf86dd; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_supported_parameters
    ADD CONSTRAINT fk1yb9u4nb6jmqx94sx08vf86dd FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: exercise_ratings fk339tptxrcqnjjhps5drdkaeju; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_ratings
    ADD CONSTRAINT fk339tptxrcqnjjhps5drdkaeju FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: personal_records fk43op0qodsbh38g025kceajw48; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.personal_records
    ADD CONSTRAINT fk43op0qodsbh38g025kceajw48 FOREIGN KEY (parameter_id) REFERENCES public.custom_parameters(id);


--
-- Name: exercise_categories fk451mdwd93mvidikrigc6lwqho; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_categories
    ADD CONSTRAINT fk451mdwd93mvidikrigc6lwqho FOREIGN KEY (owner_id) REFERENCES public.users(id);


--
-- Name: routine_import_log fk4g786k23g44ik53eiilllxpwu; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_import_log
    ADD CONSTRAINT fk4g786k23g44ik53eiilllxpwu FOREIGN KEY (imported_by) REFERENCES public.users(id);


--
-- Name: workout_sessions fk4q6pnw9nar0dxwb0qsofyefea; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.workout_sessions
    ADD CONSTRAINT fk4q6pnw9nar0dxwb0qsofyefea FOREIGN KEY (routine_id) REFERENCES public.routines(id);


--
-- Name: routines fk4ytaj1c9r47yq9u4dkcxybc32; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routines
    ADD CONSTRAINT fk4ytaj1c9r47yq9u4dkcxybc32 FOREIGN KEY (original_routine_id) REFERENCES public.routines(id);


--
-- Name: personal_records fk5si3b6ei6i5ckrtfk7ycc3j3r; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.personal_records
    ADD CONSTRAINT fk5si3b6ei6i5ckrtfk7ycc3j3r FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: user_installed_packages fk5tc95pcuxnn2p8reh223fjt2y; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.user_installed_packages
    ADD CONSTRAINT fk5tc95pcuxnn2p8reh223fjt2y FOREIGN KEY (package_id) REFERENCES public.packages(id);


--
-- Name: exercise_sports fk6a7b1srb9qrlqpik4a3js8jyl; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_sports
    ADD CONSTRAINT fk6a7b1srb9qrlqpik4a3js8jyl FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: package_items fk6aengkb578otbosx06lcx3sut; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.package_items
    ADD CONSTRAINT fk6aengkb578otbosx06lcx3sut FOREIGN KEY (parameter_id) REFERENCES public.custom_parameters(id);


--
-- Name: set_execution_parameters fk75t8m2rym4ulp5tt6tl0y28ur; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.set_execution_parameters
    ADD CONSTRAINT fk75t8m2rym4ulp5tt6tl0y28ur FOREIGN KEY (parameter_id) REFERENCES public.custom_parameters(id);


--
-- Name: progression_snapshots fk7i82r6vj2q9x9rclhjy74ekgn; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.progression_snapshots
    ADD CONSTRAINT fk7i82r6vj2q9x9rclhjy74ekgn FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: session_exercises fk7n5f7tkelbrxgc1q7ka7oacpo; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_exercises
    ADD CONSTRAINT fk7n5f7tkelbrxgc1q7ka7oacpo FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: personal_records fk7q1unia395ttscx1leghx8s58; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.personal_records
    ADD CONSTRAINT fk7q1unia395ttscx1leghx8s58 FOREIGN KEY (set_execution_id) REFERENCES public.set_executions(id);


--
-- Name: routine_exercises fk85bhq039u7fg33l5n92wqn9m; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_exercises
    ADD CONSTRAINT fk85bhq039u7fg33l5n92wqn9m FOREIGN KEY (routine_id) REFERENCES public.routines(id);


--
-- Name: exercise_category_mapping fk8hoj7cca1ka2hxn9anq7a944a; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_category_mapping
    ADD CONSTRAINT fk8hoj7cca1ka2hxn9anq7a944a FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: sports fk8ivvqursd3ffxhirkvloirkxn; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.sports
    ADD CONSTRAINT fk8ivvqursd3ffxhirkvloirkxn FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: session_metrics fk8pbbwqf3bwwixidb5slbtqk03; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_metrics
    ADD CONSTRAINT fk8pbbwqf3bwwixidb5slbtqk03 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: set_execution_parameters fk8v4wr3vkatqnrwxltwuli9vsa; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.set_execution_parameters
    ADD CONSTRAINT fk8v4wr3vkatqnrwxltwuli9vsa FOREIGN KEY (set_execution_id) REFERENCES public.set_executions(id);


--
-- Name: user_installed_packages fk8xyiej39pheu07rn4qnoihr57; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.user_installed_packages
    ADD CONSTRAINT fk8xyiej39pheu07rn4qnoihr57 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: progression_snapshots fk96qd3tvle2jf88qff89ve3wqh; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.progression_snapshots
    ADD CONSTRAINT fk96qd3tvle2jf88qff89ve3wqh FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: custom_parameters fkb3qtf002vmpk5ct8ok310afua; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.custom_parameters
    ADD CONSTRAINT fkb3qtf002vmpk5ct8ok310afua FOREIGN KEY (owner_id) REFERENCES public.users(id);


--
-- Name: exercise_metrics fkb58vhw9b01qybji1uk2fn79ek; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_metrics
    ADD CONSTRAINT fkb58vhw9b01qybji1uk2fn79ek FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: package_items fkcucis89lqmfmjdeavfpo7fyjm; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.package_items
    ADD CONSTRAINT fkcucis89lqmfmjdeavfpo7fyjm FOREIGN KEY (routine_id) REFERENCES public.routines(id);


--
-- Name: routine_import_log fkdg09rl1s8ygo427ojem0fi0qx; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_import_log
    ADD CONSTRAINT fkdg09rl1s8ygo427ojem0fi0qx FOREIGN KEY (imported_routine_id) REFERENCES public.routines(id);


--
-- Name: session_metrics fkdvp54ibljdlx7h9mrgxnh9oo0; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_metrics
    ADD CONSTRAINT fkdvp54ibljdlx7h9mrgxnh9oo0 FOREIGN KEY (session_id) REFERENCES public.workout_sessions(id);


--
-- Name: exercise_metrics fkeiig6hx1bn0vk6eqadf99qkt2; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_metrics
    ADD CONSTRAINT fkeiig6hx1bn0vk6eqadf99qkt2 FOREIGN KEY (parameter_id) REFERENCES public.custom_parameters(id);


--
-- Name: routine_exercise_parameters fkf52n9e4yhkt00tgw2a0k8cw18; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_exercise_parameters
    ADD CONSTRAINT fkf52n9e4yhkt00tgw2a0k8cw18 FOREIGN KEY (routine_exercise_id) REFERENCES public.routine_exercises(id);


--
-- Name: personal_records fkfb734lyklj943i562usa54v10; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.personal_records
    ADD CONSTRAINT fkfb734lyklj943i562usa54v10 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: exercise_supported_parameters fkfnoha2pcch5nbj7n6rln0sb6t; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_supported_parameters
    ADD CONSTRAINT fkfnoha2pcch5nbj7n6rln0sb6t FOREIGN KEY (parameter_id) REFERENCES public.custom_parameters(id);


--
-- Name: users fkfwx079xww5uyfbpi9u8gwam34; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkfwx079xww5uyfbpi9u8gwam34 FOREIGN KEY (subscription_id) REFERENCES public.subscriptions(id);


--
-- Name: routine_set_templates fkhb9qiabtffll3m43o6i7d5ygf; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_set_templates
    ADD CONSTRAINT fkhb9qiabtffll3m43o6i7d5ygf FOREIGN KEY (routine_exercise_id) REFERENCES public.routine_exercises(id);


--
-- Name: routine_exercise_parameters fkhe0a0xyomrggjihml4slnu302; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_exercise_parameters
    ADD CONSTRAINT fkhe0a0xyomrggjihml4slnu302 FOREIGN KEY (parameter_id) REFERENCES public.custom_parameters(id);


--
-- Name: subscriptions fkhro52ohfqfbay9774bev0qinr; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT fkhro52ohfqfbay9774bev0qinr FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: progression_snapshots fkhupg3u4aljxsf8l514gxjmkhm; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.progression_snapshots
    ADD CONSTRAINT fkhupg3u4aljxsf8l514gxjmkhm FOREIGN KEY (parameter_id) REFERENCES public.custom_parameters(id);


--
-- Name: set_executions fkilf4pa3d7794b92p2x6ti44ot; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.set_executions
    ADD CONSTRAINT fkilf4pa3d7794b92p2x6ti44ot FOREIGN KEY (session_exercise_id) REFERENCES public.session_exercises(id);


--
-- Name: metric_calculation_jobs fkitb1gqkychyl16d5v13tr9yj2; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.metric_calculation_jobs
    ADD CONSTRAINT fkitb1gqkychyl16d5v13tr9yj2 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: routines fkk362u96485up3k9iw6trgkvc1; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routines
    ADD CONSTRAINT fkk362u96485up3k9iw6trgkvc1 FOREIGN KEY (package_id) REFERENCES public.packages(id);


--
-- Name: exercise_metrics fkkip7ft9004ytliq5fuhlkcqpa; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_metrics
    ADD CONSTRAINT fkkip7ft9004ytliq5fuhlkcqpa FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: exercise_categories fkkn7ll8w0qwodt6gkp3ern7tkt; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_categories
    ADD CONSTRAINT fkkn7ll8w0qwodt6gkp3ern7tkt FOREIGN KEY (sport_id) REFERENCES public.sports(id);


--
-- Name: routine_exercises fkl37yvf0sdf7iqg9l7aoa9mxkq; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_exercises
    ADD CONSTRAINT fkl37yvf0sdf7iqg9l7aoa9mxkq FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: package_items fklk7ccw8ywkjidktb4627y7vbj; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.package_items
    ADD CONSTRAINT fklk7ccw8ywkjidktb4627y7vbj FOREIGN KEY (package_id) REFERENCES public.packages(id);


--
-- Name: routines fklsu2ed1i9h7w7j9i3t5nhqyka; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routines
    ADD CONSTRAINT fklsu2ed1i9h7w7j9i3t5nhqyka FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: session_metrics fkmcnswnmlbg3h5mr34boihre4v; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_metrics
    ADD CONSTRAINT fkmcnswnmlbg3h5mr34boihre4v FOREIGN KEY (routine_id) REFERENCES public.routines(id);


--
-- Name: routines fknghr8u9qr9midv5a1iackvg71; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routines
    ADD CONSTRAINT fknghr8u9qr9midv5a1iackvg71 FOREIGN KEY (sport_id) REFERENCES public.sports(id);


--
-- Name: exercise_ratings fkojegrxc1ljp2ib2tp3dkul64c; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_ratings
    ADD CONSTRAINT fkojegrxc1ljp2ib2tp3dkul64c FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: subscription_history fkowmy211t10vxtxrbaamvccnkl; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscription_history
    ADD CONSTRAINT fkowmy211t10vxtxrbaamvccnkl FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: exercises fkp2kcs08pa87cbt6duijofnivm; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercises
    ADD CONSTRAINT fkp2kcs08pa87cbt6duijofnivm FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: subscriptions fkp2p13kl6wb3vfcbf5jvik3i4p; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT fkp2p13kl6wb3vfcbf5jvik3i4p FOREIGN KEY (limits_id) REFERENCES public.subscription_limits(id);


--
-- Name: packages fkq0kjsb8ktspmxrpoia3lv762g; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.packages
    ADD CONSTRAINT fkq0kjsb8ktspmxrpoia3lv762g FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: package_items fkq40ygxgiq0hbnjan7regqlqhy; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.package_items
    ADD CONSTRAINT fkq40ygxgiq0hbnjan7regqlqhy FOREIGN KEY (category_id) REFERENCES public.exercise_categories(id);


--
-- Name: set_executions fkqh8ou2ye47j4ekhlxhs6afu4d; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.set_executions
    ADD CONSTRAINT fkqh8ou2ye47j4ekhlxhs6afu4d FOREIGN KEY (set_template_id) REFERENCES public.routine_set_templates(id);


--
-- Name: metric_calculation_jobs fkqoxfl58811as9c7d58bfhg23v; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.metric_calculation_jobs
    ADD CONSTRAINT fkqoxfl58811as9c7d58bfhg23v FOREIGN KEY (session_id) REFERENCES public.workout_sessions(id);


--
-- Name: exercise_sports fkqvpglc2e6aici0jjjwijjliq3; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.exercise_sports
    ADD CONSTRAINT fkqvpglc2e6aici0jjjwijjliq3 FOREIGN KEY (sport_id) REFERENCES public.sports(id);


--
-- Name: routine_set_parameters fkrfisx6lhpylf0nymfnil19afi; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.routine_set_parameters
    ADD CONSTRAINT fkrfisx6lhpylf0nymfnil19afi FOREIGN KEY (parameter_id) REFERENCES public.custom_parameters(id);


--
-- Name: session_exercises fkstnif8un54p6hv08ghrx3r488; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.session_exercises
    ADD CONSTRAINT fkstnif8un54p6hv08ghrx3r488 FOREIGN KEY (session_id) REFERENCES public.workout_sessions(id);


--
-- Name: package_items fktfxdtradgoxjafwee1avdt2q1; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.package_items
    ADD CONSTRAINT fktfxdtradgoxjafwee1avdt2q1 FOREIGN KEY (exercise_id) REFERENCES public.exercises(id);


--
-- Name: package_items fkwqt1nyuhn3t24765a7q6o609; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.package_items
    ADD CONSTRAINT fkwqt1nyuhn3t24765a7q6o609 FOREIGN KEY (sport_id) REFERENCES public.sports(id);


--
-- Name: spring_session_attributes spring_session_attributes_fk; Type: FK CONSTRAINT; Schema: public; Owner: jnobfit_user
--

ALTER TABLE ONLY public.spring_session_attributes
    ADD CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id) REFERENCES public.spring_session(primary_id) ON DELETE CASCADE;


--
-- jnobfit_userQL database dump complete
--

