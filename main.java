/*
 * aluminIAI v2 — synaptic crown mesh for collective inference pulses.
 * @dev lattice phase 9 / crown-drift alignment
 * Cortex lattice: 0x35C0bb6FffB2dA6dF7bB7FcBAEec6EEea8EFBd2b11EACEa84Ef1Ef36873a605E
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * aluminIAI v2 — off-chain collective intelligence: cortex cells, synapse lanes,
 * pulse registry, ballot rings, memory spindles, and mainnet attestation envelopes.
 */
public final class aluminIAI {

    public static final String ENGINE_LABEL = "aluminIAI";
    public static final String RELEASE_TAG = "synapse-crown-v2.1";
    public static final int MAX_CORTEX_CELLS = 640;
    public static final int MAX_PULSE_SLOTS = 3072;
    public static final int MAX_SYNAPSE_LANES = 1536;
    public static final int MAX_BALLOT_RINGS = 384;
    public static final int MAX_SPINDLE_RECORDS = 896;
    public static final int MAX_DISPATCH_QUEUE = 192;
    public static final int METRIC_BUFFER_CAP = 6144;
    public static final int ATTESTATION_TTL_SECONDS = 129600;
    public static final int STIPEND_BASIS_POINTS = 71;
    public static final long BPS_DENOMINATOR = 10_000L;
    public static final long DEFAULT_CHAIN_ID = 1L;
    public static final String DOMAIN_SEPARATOR = "aluminIAI_synapse_crown_v2";
    public static final String DIGEST_ALGORITHM = "SHA-256";
    public static final int MIN_RING_WEIGHT = 5;
    public static final int MAX_RING_WEIGHT = 88;

    public static final String ADDRESS_A = "0x194f507d94D4dE972510761Cbc5a978253f3949D";
    public static final String ADDRESS_B = "0x8037Bc2F288EC52Cd20C188aAe9f3f41Ec154956";
    public static final String ADDRESS_C = "0x8A0682D9fb37372194b087dD1e26865B510fCdF8";
    public static final String ADDRESS_D = "0xe9261d44EDF33595458c021a0794B29a707aD20A";
    public static final String ADDRESS_E = "0x5bBbBfA48AC9BB25439F267F2b59C16dB784a914";
    public static final String ADDRESS_F = "0xE8958fC3d771067Fc6edf864Cbdf1e600336a107";
    public static final String ADDRESS_G = "0x9467F80E3A8223f54857E2705B8C4e1F1102332d";
    public static final String ADDRESS_H = "0x5823D3Af6078ea92cE69bA346De98B6CDd4e5F18";
    public static final String SYNAPSE_DOMAIN_HEX = "0x97fC6FE2B0BdCfd9FB7B8d0Dc50ad7F13BeD31fAF299df40fA83294fa0cE2bc4";
    public static final String PULSE_SALT_HEX = "0x4B9DdE058DA95e4Bd16683757F6BEb85fc1D0fEA15f0c4708E5d9E43d8915F3F";
    public static final String CORTEX_SEED_HEX = "0x35C0bb6FffB2dA6dF7bB7FcBAEec6EEea8EFBd2b11EACEa84Ef1Ef36873a605E";
    public static final String COLLECTIVE_ANCHOR_HEX = "0xCEeAaCDDBA9237926CE73AFAF7D2fe7123B219364F8Aa46CBDCEda535CdaE122";

    private final CortexRuntimeConfig runtimeConfig;
    private final CortexCellRegistry cortexCellRegistry;
    private final PulseRegistry pulseRegistry;
    private final SynapseLaneBook synapseLaneBook;
    private final BallotRingEngine ballotRingEngine;
    private final SpindleArchive spindleArchive;
    private final DispatchQueue dispatchQueue;
    private final EnvelopeRelay envelopeRelay;
    private final CollectiveJournal collectiveJournal;
    private final PulseMetricsBuffer pulseMetricsBuffer;
    private final CortexGate cortexGate;
    private final CrownReportRenderer crownReportRenderer;
    private final AtomicBoolean lanePaused;
    private final AtomicLong lineEpoch;
    private final Instant bootInstant;

    public aluminIAI(CortexRuntimeConfig runtimeConfig) {
        this.runtimeConfig = Objects.requireNonNull(runtimeConfig, "runtimeConfig");
        this.cortexCellRegistry = new CortexCellRegistry(MAX_CORTEX_CELLS);
        this.pulseRegistry = new PulseRegistry(MAX_PULSE_SLOTS);
        this.synapseLaneBook = new SynapseLaneBook(MAX_SYNAPSE_LANES);
        this.ballotRingEngine = new BallotRingEngine(MAX_BALLOT_RINGS, MIN_RING_WEIGHT, MAX_RING_WEIGHT);
        this.spindleArchive = new SpindleArchive(MAX_SPINDLE_RECORDS);
        this.dispatchQueue = new DispatchQueue(MAX_DISPATCH_QUEUE);
        this.envelopeRelay = new EnvelopeRelay(runtimeConfig);
        this.collectiveJournal = new CollectiveJournal();
        this.pulseMetricsBuffer = new PulseMetricsBuffer(METRIC_BUFFER_CAP);
        this.cortexGate = new CortexGate();
        this.crownReportRenderer = new CrownReportRenderer();
        this.lanePaused = new AtomicBoolean(false);
        this.lineEpoch = new AtomicLong(0L);
        this.bootInstant = Instant.now();
    }

    public static aluminIAI bootstrapDefault() {
        CortexRuntimeConfig cfg = new CortexRuntimeConfig(
                DEFAULT_CHAIN_ID,
                ADDRESS_A,
                ADDRESS_B,
                ADDRESS_C,
                ADDRESS_D,
                ADDRESS_E,
                SYNAPSE_DOMAIN_HEX,
                RELEASE_TAG
        );
        return new aluminIAI(cfg);
    }

    public CortexRuntimeConfig getRuntimeConfig() { return runtimeConfig; }
    public CortexCellRegistry cells() { return cortexCellRegistry; }
    public PulseRegistry pulses() { return pulseRegistry; }
    public SynapseLaneBook lanes() { return synapseLaneBook; }
    public BallotRingEngine ballots() { return ballotRingEngine; }
    public SpindleArchive spindles() { return spindleArchive; }
    public DispatchQueue dispatch() { return dispatchQueue; }
    public EnvelopeRelay envelopes() { return envelopeRelay; }
    public CollectiveJournal journal() { return collectiveJournal; }
    public PulseMetricsBuffer metrics() { return pulseMetricsBuffer; }
    public CortexGate gate() { return cortexGate; }
    public CrownReportRenderer reports() { return crownReportRenderer; }

    public boolean isLanePaused() { return lanePaused.get(); }

    public void setLanePaused(boolean paused, String actorAddress) {
        cortexGate.requireGovernor(actorAddress, runtimeConfig.getGovernorAddress());
        lanePaused.set(paused);
        collectiveJournal.record(new JournalEntry(
                paused ? "Paused" : "Resumed",
                actorAddress,
                lineEpoch.get(),
                Instant.now()
        ));
    }

    public void transferPitMaster(String nextPitMaster, String actorAddress) {
        cortexGate.requireGovernor(actorAddress, runtimeConfig.getGovernorAddress());
        cortexGate.requireValidAddress(nextPitMaster);
        runtimeConfig.assignPitMaster(nextPitMaster);
        collectiveJournal.record(new JournalEntry(
                "PitMasterMoved",
                actorAddress,
                lineEpoch.get(),
                Instant.now(),
                Map.of("next", nextPitMaster.trim())
        ));
    }

    public long tickLineEpoch() {
        long next = lineEpoch.incrementAndGet();
        pulseMetricsBuffer.recordGauge("lineEpoch", next);
        return next;
    }

    public long currentLineEpoch() { return lineEpoch.get(); }
    public Instant getBootInstant() { return bootInstant; }

    public void requireLiveLane() {
        if (lanePaused.get()) {
            throw new NiAl_LanePausedException();
        }
    }

    public String computeSplitDigest(String laneTag, String pulseKey, byte[] payload) {
        try {
            MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
            md.update(runtimeConfig.getDomainSeed());
            md.update(laneTag.getBytes(StandardCharsets.UTF_8));
            md.update(pulseKey.getBytes(StandardCharsets.UTF_8));
            if (payload != null) {
                md.update(payload);
            }
            byte[] hA = md.digest();
            md.reset();
            md.update(hA);
            md.update(PULSE_SALT_HEX.getBytes(StandardCharsets.UTF_8));
            md.update(ByteBuffer.allocate(8).putLong(runtimeConfig.getChainId()).array());
            byte[] hB = md.digest();
            byte[] packed = new byte[hA.length + hB.length];
            System.arraycopy(hA, 0, packed, 0, hA.length);
            System.arraycopy(hB, 0, packed, hA.length, hB.length);
            return "0x" + HexFormat.of().formatHex(packed);
        } catch (NoSuchAlgorithmException e) {
            throw new NiAl_DigestFailureException(e);
        }
    }

    public Map<String, Object> buildHealthSnapshot() {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("engine", ENGINE_LABEL);
        snap.put("release", RELEASE_TAG);
        snap.put("chainId", runtimeConfig.getChainId());
        snap.put("lineEpoch", lineEpoch.get());
        snap.put("lanePaused", lanePaused.get());
        snap.put("cortexCells", cortexCellRegistry.size());
        snap.put("pulses", pulseRegistry.size());
        snap.put("synapseLanes", synapseLaneBook.size());
        snap.put("openBallots", ballotRingEngine.openCount());
        snap.put("spindles", spindleArchive.size());
        snap.put("pendingDispatch", dispatchQueue.pendingCount());
        snap.put("bootUtc", bootInstant.toString());
        snap.put("metricSamples", pulseMetricsBuffer.sampleCount());
        return snap;
    }

    // --- Runtime configuration ---

    public static final class CortexRuntimeConfig {
        private final long chainId;
        private final String governorAddress;
        private String pitMasterAddress;
        private final String signalOracleAddress;
        private final String relayAddress;
        private final String envelopeSinkAddress;
        private final String synapseDomainHex;
        private final String versionTag;
        private final byte[] domainSeed;

        public CortexRuntimeConfig(
                long chainId,
                String governorAddress,
                String pitMasterAddress,
                String signalOracleAddress,
                String relayAddress,
                String envelopeSinkAddress,
                String synapseDomainHex,
                String versionTag
        ) {
            this.chainId = chainId;
            this.governorAddress = normalizeAddress(governorAddress);
            this.pitMasterAddress = normalizeAddress(pitMasterAddress);
            this.signalOracleAddress = normalizeAddress(signalOracleAddress);
            this.relayAddress = normalizeAddress(relayAddress);
            this.envelopeSinkAddress = normalizeAddress(envelopeSinkAddress);
            this.synapseDomainHex = synapseDomainHex == null ? "" : synapseDomainHex.trim();
            this.versionTag = versionTag == null ? RELEASE_TAG : versionTag;
            this.domainSeed = buildDomainSeed(this.chainId, this.synapseDomainHex, this.versionTag);
        }

        private static byte[] buildDomainSeed(long chainId, String domainHex, String version) {
            try {
                MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
                md.update(DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8));
                md.update(ByteBuffer.allocate(8).putLong(chainId).array());
                md.update(domainHex.getBytes(StandardCharsets.UTF_8));
                md.update(version.getBytes(StandardCharsets.UTF_8));
                return md.digest();
            } catch (NoSuchAlgorithmException e) {
                throw new NiAl_DigestFailureException(e);
            }
        }

        private static String normalizeAddress(String addr) {
            if (addr == null || addr.isBlank()) {
                throw new NiAl_InvalidAddressException("empty");
            }
            String trimmed = addr.trim();
            if (!trimmed.startsWith("0x") || trimmed.length() != 42) {
                throw new NiAl_InvalidAddressException(trimmed);
            }
            return trimmed;
        }

        void assignPitMaster(String next) {
            this.pitMasterAddress = normalizeAddress(next);
        }

        public long getChainId() { return chainId; }
        public String getGovernorAddress() { return governorAddress; }
        public String getPitMasterAddress() { return pitMasterAddress; }
        public String getSignalOracleAddress() { return signalOracleAddress; }
        public String getRelayAddress() { return relayAddress; }
        public String getEnvelopeSinkAddress() { return envelopeSinkAddress; }
        public String getSynapseDomainHex() { return synapseDomainHex; }
        public String getVersionTag() { return versionTag; }
        public byte[] getDomainSeed() { return Arrays.copyOf(domainSeed, domainSeed.length); }
    }

    public static class NiAl_LanePausedException extends RuntimeException {
        public NiAl_LanePausedException() { super("NiAl: lane paused"); }
    }

    public static class NiAl_CapacityExceededException extends RuntimeException {
        public NiAl_CapacityExceededException(String detail) {
            super("NiAl: capacity — " + detail);
        }
    }

    public static class NiAl_NotFoundException extends RuntimeException {
        public NiAl_NotFoundException(String id) {
            super("NiAl: not found — " + id);
        }
    }

    public static class NiAl_InvalidAddressException extends RuntimeException {
        public NiAl_InvalidAddressException(String addr) {
            super("NiAl: bad address — " + addr);
        }
    }

    public static class NiAl_UnauthorizedException extends RuntimeException {
        public NiAl_UnauthorizedException() { super("NiAl: unauthorized"); }
    }

    public static class NiAl_DigestFailureException extends RuntimeException {
        public NiAl_DigestFailureException(Throwable cause) {
            super("NiAl: digest failure", cause);
        }
    }

    public static class NiAl_RingOpenException extends RuntimeException {
        public NiAl_RingOpenException(long ringId) {
            super("NiAl: ring not settled — " + ringId);
        }
    }

    public static class NiAl_LaneDormantException extends RuntimeException {
        public NiAl_LaneDormantException(long laneId) {
            super("NiAl: lane dormant — " + laneId);
        }
    }

    public enum CellKind { PROBE, FORGE, FUSION, ORACLE_TIE }

    public enum CellState { IDLE, ACTIVE, ISOLATED, RETIRED }

    public static final class CortexCellRecord {
        public final long cellId;
        public final String label;
        public final String operatorAddress;
        public final CellKind kind;
        public final int torque;
        public final Instant enlistedAt;
        public CellState state;
        public long lastPulseEpoch;
        public final List<Long> boundPulseIds;

        public CortexCellRecord(long cellId, String label, String operatorAddress, CellKind kind, int torque) {
            this.cellId = cellId;
            this.label = label == null ? "cell-" + cellId : label;
            this.operatorAddress = operatorAddress;
            this.kind = kind == null ? CellKind.FORGE : kind;
            this.torque = Math.max(1, Math.min(48, torque));
            this.enlistedAt = Instant.now();
            this.state = CellState.IDLE;
            this.lastPulseEpoch = 0L;
            this.boundPulseIds = new ArrayList<>();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("cellId", cellId);
            m.put("label", label);
            m.put("operator", operatorAddress);
            m.put("kind", kind.name());
            m.put("torque", torque);
            m.put("state", state.name());
            m.put("pulses", boundPulseIds.size());
            return m;
        }
    }

    public static final class CortexCellRegistry {
        private final int capacity;
        private final AtomicLong idSeq = new AtomicLong(0L);
        private final Map<Long, CortexCellRecord> cells = new ConcurrentHashMap<>();

        public CortexCellRegistry(int capacity) {
            this.capacity = Math.max(1, capacity);
        }

        public int size() { return cells.size(); }

        public long enlist(String label, String operator, CellKind kind, int torque) {
            if (cells.size() >= capacity) {
                throw new NiAl_CapacityExceededException("cortex cells");
            }
            long id = idSeq.incrementAndGet();
            cells.put(id, new CortexCellRecord(id, label, operator, kind, torque));
            return id;
        }

        public CortexCellRecord requireCell(long cellId) {
            CortexCellRecord c = cells.get(cellId);
            if (c == null) {
                throw new NiAl_NotFoundException("cell:" + cellId);
            }
            return c;
        }

        public void pulseTouch(long cellId, long epoch) {
            CortexCellRecord c = requireCell(cellId);
            c.lastPulseEpoch = epoch;
            c.state = CellState.ACTIVE;
        }

        public void isolate(long cellId) {
            requireCell(cellId).state = CellState.ISOLATED;
        }

        public int totalTorque() {
            return cells.values().stream()
                    .filter(c -> c.state != CellState.RETIRED && c.state != CellState.ISOLATED)
                    .mapToInt(c -> c.torque)
                    .sum();
        }

        public Map<Long, CortexCellRecord> snapshot() {
            return Collections.unmodifiableMap(new TreeMap<>(cells));
        }
    }

    public enum PulseStage { RAW, POLISHED, CROWN_READY, SEALED }

    public static final class PulseSlot {
        public final long pulseId;
        public final long lineId;
        public final String laneTag;
        public final String contentDigest;
        public final String authorAddress;
        public final Instant emittedAt;
        public PulseStage stage;
        public int polishScore;

        public PulseSlot(long pulseId, long lineId, String laneTag, String contentDigest, String authorAddress) {
            this.pulseId = pulseId;
            this.lineId = lineId;
            this.laneTag = laneTag == null ? "default" : laneTag;
            this.contentDigest = contentDigest == null ? "" : contentDigest;
            this.authorAddress = authorAddress;
            this.emittedAt = Instant.now();
            this.stage = PulseStage.RAW;
            this.polishScore = 0;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("pulseId", pulseId);
            m.put("lineId", lineId);
            m.put("laneTag", laneTag);
            m.put("digest", contentDigest);
            m.put("stage", stage.name());
            m.put("polish", polishScore);
            return m;
        }
    }

    public static final class PulseRegistry {
        private final int capacity;
        private final AtomicLong idSeq = new AtomicLong(0L);
        private final AtomicLong lineSeq = new AtomicLong(0L);
        private final Map<Long, PulseSlot> slots = new ConcurrentHashMap<>();
        private final Map<Long, List<Long>> byLineId = new ConcurrentHashMap<>();

        public PulseRegistry(int capacity) {
            this.capacity = Math.max(1, capacity);
        }

        public int size() { return slots.size(); }

        public long emit(String laneTag, String contentDigest, String authorAddress) {
            if (slots.size() >= capacity) {
                throw new NiAl_CapacityExceededException("pulse slots");
            }
            long lineId = lineSeq.incrementAndGet();
            long id = idSeq.incrementAndGet();
            PulseSlot slot = new PulseSlot(id, lineId, laneTag, contentDigest, authorAddress);
            slots.put(id, slot);
            byLineId.computeIfAbsent(lineId, k -> new CopyOnWriteArrayList<>()).add(id);
            return id;
        }

        public PulseSlot requirePulse(long pulseId) {
            PulseSlot s = slots.get(pulseId);
            if (s == null) {
                throw new NiAl_NotFoundException("pulse:" + pulseId);
            }
            return s;
        }

        public void polish(long pulseId, int delta) {
            PulseSlot s = requirePulse(pulseId);
            s.polishScore = Math.min(900, s.polishScore + delta);
            if (s.polishScore >= 140) s.stage = PulseStage.POLISHED;
            if (s.polishScore >= 520) s.stage = PulseStage.CROWN_READY;
        }

        public List<PulseSlot> listByLine(long lineId) {
            return byLineId.getOrDefault(lineId, List.of()).stream()
                    .map(slots::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        public List<PulseSlot> crownReady() {
            return slots.values().stream()
                    .filter(p -> p.stage == PulseStage.CROWN_READY)
                    .sorted(Comparator.comparingLong(p -> p.pulseId))
                    .collect(Collectors.toList());
        }
    }

    public static final class SynapseLaneEntry {
        public final long laneId;
        public final long fromCellId;
        public final long toCellId;
        public final String signalDigest;
        public final int amplitude;
        public final long expiryLineEpoch;
        public boolean dormant;

        public SynapseLaneEntry(long laneId, long fromCellId, long toCellId, String signalDigest, int amplitude, long expiryLineEpoch) {
            this.laneId = laneId;
            this.fromCellId = fromCellId;
            this.toCellId = toCellId;
            this.signalDigest = signalDigest == null ? "" : signalDigest;
            this.amplitude = Math.max(1, Math.min(240, amplitude));
