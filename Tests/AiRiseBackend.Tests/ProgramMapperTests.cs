using AiRise.Services;   // ProgramTypeMapper
using AiRise.Models;     // ProgramType
using FluentAssertions;

public class ProgramTypeMapperTests
{
    [Theory]
    [InlineData("gym", ProgramType.Gym)]
    [InlineData("home", ProgramType.HomeDumbbell)]
    [InlineData("dumbbell", ProgramType.HomeDumbbell)]
    [InlineData("bodyweight", ProgramType.Bodyweight)]
    [InlineData("home, gym", ProgramType.Gym)]
    [InlineData("", ProgramType.Bodyweight)]
    public void MapEquipmentToProgramType_Works(string input, ProgramType expected)
    {
        ProgramTypeMapper.MapEquipmentToProgramType(input).Should().Be(expected);
    }
}
